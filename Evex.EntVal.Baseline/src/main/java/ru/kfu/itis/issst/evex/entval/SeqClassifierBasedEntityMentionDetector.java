/**
 * 
 */
package ru.kfu.itis.issst.evex.entval;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static ru.kfu.itis.cll.uima.cas.AnnotationUtils.coveredTextFunction;
import static ru.kfu.itis.cll.uima.cas.AnnotationUtils.toPrettyString;
import static ru.kfu.itis.cll.uima.util.DocumentUtils.getDocumentUri;
import static ru.kfu.itis.issst.uima.ml.DefaultFeatureExtractors.currentTokenExtractors;
import static ru.kfu.itis.issst.uima.ml.DefaultFeatureExtractors.contextTokenExtractors;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instances;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.ExternalResource;
import org.uimafit.factory.AnnotationFactory;
import org.uimafit.util.ContainmentIndex;
import org.uimafit.util.JCasUtil;

import ru.kfu.cll.uima.segmentation.fstype.Sentence;
import ru.kfu.cll.uima.tokenizer.fstype.Token;
import ru.kfu.itis.issst.cleartk.Disposable;
import ru.kfu.itis.issst.cleartk.JarSequenceClassifierFactory;
import ru.kfu.itis.issst.cleartk.crfsuite.CRFSuiteStringOutcomeDataWriterFactory;
import ru.kfu.itis.issst.uima.ml.GrammemeExtractor;
import ru.kfu.itis.issst.uima.ml.LemmaFeatureExtractor;
import ru.kfu.itis.issst.uima.ml.NGCAgreementFeatureExtractor;
import ru.kfu.itis.issst.uima.ml.PosTagFeatureExtractor;
import ru.kfu.itis.issst.uima.morph.dictionary.resource.GramModel;
import ru.kfu.itis.issst.uima.morph.dictionary.resource.GramModelHolder;
import ru.kfu.itis.issst.uima.morph.model.MorphConstants;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class SeqClassifierBasedEntityMentionDetector<ET extends Annotation>
		extends CleartkSequenceAnnotator<String> {

	public static final String RESOURCE_GRAM_MODEL = "gramModel";
	public static final String PARAM_ENTITY_TYPE = "entityType";
	// BIO
	private static final String LABEL_BEGIN_PREFIX = "B_";
	private static final String LABEL_INSIDE_PREFIX = "I_";
	private static final String LABEL_OUTSIDE = "O";

	public static AnalysisEngineDescription createFeatureExtractorDescription(
			Class<?> entityTypeClass, ExternalResourceDescription gramModelDesc, File trainingDir)
			throws ResourceInitializationException {
		return createPrimitiveDescription(SeqClassifierBasedEntityMentionDetector.class,
				RESOURCE_GRAM_MODEL, gramModelDesc,
				PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
				CRFSuiteStringOutcomeDataWriterFactory.class.getName(),
				DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, trainingDir,
				PARAM_ENTITY_TYPE, entityTypeClass.getName());
	}

	public static AnalysisEngineDescription createAnalyzerDescription(
			Class<?> entityTypeClass, ExternalResourceDescription gramModelDesc, File modelDir)
			throws ResourceInitializationException {
		File modelJarFile = JarClassifierBuilder.getModelJarFile(modelDir);
		// make model jar path relative to modelBaseDir
		String jarRelativePath = relativize(modelDir, modelJarFile);
		return createPrimitiveDescription(SeqClassifierBasedEntityMentionDetector.class,
				RESOURCE_GRAM_MODEL, gramModelDesc,
				PARAM_CLASSIFIER_FACTORY_CLASS_NAME, JarSequenceClassifierFactory.class.getName(),
				JarSequenceClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, jarRelativePath,
				PARAM_ENTITY_TYPE, entityTypeClass.getName());
	}

	/**
	 * @param baseDir
	 * @param target
	 * @return relative path of target against baseDir
	 */
	private static final String relativize(File baseDir, File target) {
		// TODO:LOW use File#relativize after migration on Java 7
		// this solution will work well only when target is in baseDir,
		// but this is enough in the context of this class
		URI relativeUri = baseDir.toURI().relativize(target.toURI());
		return FilenameUtils.separatorsToSystem(relativeUri.getPath());
	}

	@ExternalResource(key = RESOURCE_GRAM_MODEL, mandatory = true)
	private GramModelHolder gramModelHolder;
	@ConfigurationParameter(name = PARAM_ENTITY_TYPE, mandatory = true)
	private Class<ET> entityTypeClass;
	private List<SimpleFeatureExtractor> simpleFeatExtractors;
	private List<CleartkExtractor> contextFeatExtractors;
	// derived
	private GramModel gramModel;
	private String beginLabel;
	private String insideLabel;

	@Override
	public void initialize(UimaContext ctx) throws ResourceInitializationException {
		super.initialize(ctx);
		gramModel = gramModelHolder.getGramModel();
		//
		beginLabel = LABEL_BEGIN_PREFIX + entityTypeClass.getSimpleName();
		insideLabel = LABEL_INSIDE_PREFIX + entityTypeClass.getSimpleName();
		//
		simpleFeatExtractors = Lists.newLinkedList();
		contextFeatExtractors = Lists.newLinkedList();
		//
		SimpleFeatureExtractor tokenFE = new CombinedExtractor(currentTokenExtractors().toArray(
				new SimpleFeatureExtractor[0]));
		simpleFeatExtractors.add(tokenFE);
		//
		SimpleFeatureExtractor lemmaFE = new LemmaFeatureExtractor();
		simpleFeatExtractors.add(lemmaFE);
		//
		SimpleFeatureExtractor tagExtractor = new PosTagFeatureExtractor();
		simpleFeatExtractors.add(tagExtractor);
		//
		SimpleFeatureExtractor posExtractor = new GrammemeExtractor(gramModel, MorphConstants.POST);
		simpleFeatExtractors.add(posExtractor);
		//
		SimpleFeatureExtractor agrExtractor = new NGCAgreementFeatureExtractor(gramModel);
		simpleFeatExtractors.add(agrExtractor);
		//
		List<SimpleFeatureExtractor> contextTokenFeatureExtractors = contextTokenExtractors();
		contextTokenFeatureExtractors.add(lemmaFE);
		contextTokenFeatureExtractors.add(tagExtractor);
		contextTokenFeatureExtractors.add(posExtractor);
		SimpleFeatureExtractor contextTokenFeatureExtractor = new CombinedExtractor(
				contextTokenFeatureExtractors.toArray(
						new SimpleFeatureExtractor[contextTokenFeatureExtractors.size()]));
		CleartkExtractor contextTokenFE = new CleartkExtractor(Token.class,
				contextTokenFeatureExtractor,
				new CleartkExtractor.Preceding(2),
				new CleartkExtractor.Following(2));
		contextFeatExtractors.add(contextTokenFE);
	}

	@Override
	public void destroy() {
		if (classifier instanceof Disposable) {
			((Disposable) classifier).dispose();
		}
		super.destroy();
	}

	// per-CAS state fields
	private ContainmentIndex<ET, Token> entityTokenContIndex;

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		if (isTraining()) {
			entityTokenContIndex = ContainmentIndex.create(jCas, entityTypeClass, Token.class,
					ContainmentIndex.Type.REVERSE);
		}
		try {
			for (Sentence sent : JCasUtil.select(jCas, Sentence.class)) {
				process(jCas, sent);
			}
		} finally {
			entityTokenContIndex = null;
		}
	}

	private void process(JCas jCas, Sentence sent) throws AnalysisEngineProcessException {
		if (isTraining()) {
			trainingProcess(jCas, sent);
		} else {
			taggingProcess(jCas, sent);
		}
	}

	private void trainingProcess(JCas jCas, final Sentence sent) throws CleartkProcessingException {
		List<List<Feature>> sentSeq = Lists.newArrayList();
		List<String> sentLabels = Lists.newArrayList();
		ET lastEntity = null;
		for (Token token : JCasUtil.selectCovered(jCas, Token.class, sent)) {
			// classification label
			String outputLabel;
			ET tokenEntity = getContainingEntity(token);
			if (tokenEntity == null) {
				outputLabel = LABEL_OUTSIDE;
			} else {
				// HERE tokenEntity != null
				// SO either B or I is expected
				if (Objects.equal(lastEntity, tokenEntity)) {
					// continue the current entity mention
					outputLabel = insideLabel;
				} else {
					// starts new entity
					outputLabel = beginLabel;
				}
			}
			lastEntity = tokenEntity;
			sentLabels.add(outputLabel);
			List<Feature> tokFeatures = extractFeatures(jCas, sent, token);
			sentSeq.add(tokFeatures);
		}
		dataWriter.write(Instances.toInstances(sentLabels, sentSeq));
	}

	private void taggingProcess(JCas jCas, final Sentence sent)
			throws AnalysisEngineProcessException {
		List<List<Feature>> sentSeq = Lists.newArrayList();
		List<Token> sentTokens = JCasUtil.selectCovered(jCas, Token.class, sent);
		for (Token token : sentTokens) {
			List<Feature> tokFeatures = extractFeatures(jCas, sent, token);
			sentSeq.add(tokFeatures);
		}
		List<String> sentLabels = classifier.classify(sentSeq);
		// sanity check
		if (sentLabels.size() != sentTokens.size()) {
			throw new IllegalStateException();
		}
		//
		if (!(sentLabels instanceof RandomAccess)) {
			sentLabels = new ArrayList<String>(sentLabels);
		}
		ET currentEntity = null;
		for (int i = 0; i < sentLabels.size(); i++) {
			Token token = sentTokens.get(i);
			String label = sentLabels.get(i);
			if (beginLabel.equals(label)) {
				// complete a last entity if any
				if (currentEntity != null) {
					currentEntity.addToIndexes();
					currentEntity = null;
				}
				// create new
				currentEntity = newEntityFrom(jCas, token);
			} else if (insideLabel.equals(label)) {
				if (currentEntity == null) {
					getLogger()
							.warn(String
									.format(
											"Illegal INSIDE label in classification result of the sentence in %s: input:\n%s\nOutput:\n%s",
											getDocumentUri(jCas),
											Lists.transform(sentTokens, coveredTextFunction()),
											sentLabels));
					// create new
					currentEntity = newEntityFrom(jCas, token);
				} else {
					// add token to entity
					addTokenToEntity(currentEntity, token);
				}
			} else if (LABEL_OUTSIDE.equals(label)) {
				// complete a last entity if any
				if (currentEntity != null) {
					currentEntity.addToIndexes();
					currentEntity = null;
				}
			} else {
				throw new IllegalStateException(String.format(
						"Got unknown classification label: %s",
						label));
			}
		}
		// handle the case when the last token has I or B 
		if (currentEntity != null) {
			currentEntity.addToIndexes();
			currentEntity = null;
		}
	}

	private List<Feature> extractFeatures(JCas jCas, final Sentence sent, final Token token)
			throws CleartkExtractorException {
		List<Feature> features = Lists.newLinkedList();
		for (SimpleFeatureExtractor fe : simpleFeatExtractors) {
			features.addAll(fe.extract(jCas, token));
		}
		for (CleartkExtractor fe : contextFeatExtractors) {
			features.addAll(fe.extractWithin(jCas, token, sent));
		}
		return features;
	}

	private ET getContainingEntity(Token token) {
		Collection<ET> entityCol = entityTokenContIndex.containing(token);
		if (entityCol.size() > 1) {
			throw new IllegalStateException(String.format(
					"Several entities cover token %s in %s",
					toPrettyString(token), getDocumentUri(token.getCAS())));
		}
		Iterator<ET> entIter = entityCol.iterator();
		if (entIter.hasNext()) {
			return entIter.next();
		} else {
			return null;
		}
	}

	private ET newEntityFrom(JCas jCas, Token token) throws AnalysisEngineProcessException {
		return createAnnotation(jCas, token.getBegin(), token.getEnd(), entityTypeClass);
	}

	private void addTokenToEntity(ET entity, Token token) {
		entity.setEnd(token.getEnd());
	}

	private static <AT extends Annotation> AT createAnnotation(JCas jCas, int begin, int end,
			Class<AT> annoClass)
			throws AnalysisEngineProcessException {
		try {
			return AnnotationFactory.createAnnotation(jCas, begin, end, annoClass);
		} catch (UIMAException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}
