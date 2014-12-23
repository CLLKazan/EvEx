/**
 * 
 */
package ru.kfu.itis.issst.evex.event.maxent;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.AnnotationFactory.createAnnotation;
import static ru.kfu.itis.cll.uima.cas.AnnotationUtils.toPrettyString;
import static ru.kfu.itis.issst.uima.ml.DefaultFeatureExtractors.contextTokenExtractors;
import static ru.kfu.itis.issst.uima.ml.DefaultFeatureExtractors.currentTokenExtractors;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.uimafit.descriptor.ExternalResource;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ru.kfu.cll.uima.segmentation.fstype.Sentence;
import ru.kfu.cll.uima.tokenizer.fstype.Token;
import ru.kfu.itis.issst.evex.Event;
import ru.kfu.itis.issst.evex.event.EventTriggerCandidate;
import ru.kfu.itis.issst.uima.ml.GrammemeExtractor;
import ru.kfu.itis.issst.uima.ml.LemmaFeatureExtractor;
import ru.kfu.itis.issst.uima.ml.PosTagFeatureExtractor;
import ru.kfu.itis.issst.uima.morph.dictionary.resource.GramModel;
import ru.kfu.itis.issst.uima.morph.dictionary.resource.GramModelHolder;
import ru.kfu.itis.issst.uima.morph.model.MorphConstants;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class MulticlassClassifierBasedEventTriggerDetector extends CleartkAnnotator<String> {

	public static AnalysisEngineDescription createFeatureExtractorDescription(File trainingDir)
			throws ResourceInitializationException {
		AnalysisEngineDescription result = createPrimitiveDescription(
				MulticlassClassifierBasedEventTriggerDetector.class,
				PARAM_DATA_WRITER_FACTORY_CLASS_NAME, DirectoryDataWriterFactory.class,
				DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, trainingDir);
		result.getAnalysisEngineMetaData().getOperationalProperties()
				.setMultipleDeploymentAllowed(false);
		return result;
	}

	public static final String RESOURCE_GRAM_MODEL = "gramModel";

	@ExternalResource(key = RESOURCE_GRAM_MODEL, mandatory = true)
	private GramModelHolder gramModelHolder;

	private List<SimpleFeatureExtractor> simpleFeatExtractors;
	private List<CleartkExtractor> contextFeatExtractors;
	// derived
	private GramModel gramModel;
	// state
	private Map<String, Class<? extends Event>> label2EventTypeMap;

	@Override
	public void initialize(UimaContext ctx) throws ResourceInitializationException {
		super.initialize(ctx);
		gramModel = gramModelHolder.getGramModel();
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
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		try {
			initLabel2EventTypeMap(jCas.getTypeSystem());
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
		Map<EventTriggerCandidate, Collection<Sentence>> sentCoveringIdx =
				JCasUtil.indexCovering(jCas, EventTriggerCandidate.class, Sentence.class);
		for (EventTriggerCandidate triggerCand : JCasUtil.select(jCas, EventTriggerCandidate.class)) {
			Collection<Sentence> _sents = sentCoveringIdx.get(triggerCand);
			if (_sents.isEmpty()) {
				getLogger().warn("Trigger candidate is outside of a sentence!");
				continue;
			}
			if (_sents.size() > 1) {
				getLogger().warn("Trigger candidate is inside of two or more sentences!");
				continue;
			}
			Sentence sent = _sents.iterator().next();
			process(jCas, triggerCand, sent);
		}
	}

	private void initLabel2EventTypeMap(TypeSystem ts) throws ClassNotFoundException {
		label2EventTypeMap = Maps.newHashMap();
		Type evBaseType = ts.getType(Event.class.getName());
		if (evBaseType == null) {
			throw new IllegalStateException();
		}
		for (Type eventType : ts.getDirectSubtypes(evBaseType)) {
			@SuppressWarnings("unchecked")
			Class<? extends Event> eventClass = (Class<? extends Event>)
					Class.forName(eventType.getName());
			label2EventTypeMap.put(eventType.getShortName(), eventClass);
		}
		label2EventTypeMap = ImmutableMap.copyOf(label2EventTypeMap);
	}

	private void process(JCas jCas, EventTriggerCandidate trigCand, Sentence sent)
			throws AnalysisEngineProcessException {
		if (isTraining()) {
			trainingProcess(jCas, trigCand, sent);
		} else {
			taggingProcess(jCas, trigCand, sent);
		}
	}

	private void trainingProcess(
			JCas jCas, final EventTriggerCandidate trigCand, final Sentence sent)
			throws CleartkProcessingException {
		// determine label
		final String label = getEventTypeLabel(jCas, trigCand);
		// trigger token
		Token trigCandToken = (Token) trigCand.getToken();
		// extract features
		List<Feature> featureVals = extractFeatures(jCas, sent, trigCandToken);
		dataWriter.write(new Instance<String>(label, featureVals));
	}

	private void taggingProcess(
			JCas jCas, final EventTriggerCandidate trigCand, final Sentence sent)
			throws AnalysisEngineProcessException {
		// trigger token
		Token trigCandToken = (Token) trigCand.getToken();
		// extract features
		List<Feature> featureVals = extractFeatures(jCas, sent, trigCandToken);
		// predicted type label
		String predTypeLabel = classifier.classify(featureVals);
		if (predTypeLabel != null && !predTypeLabel.equalsIgnoreCase("null")) {
			Class<? extends Event> eventType = label2EventTypeMap.get(predTypeLabel);
			if (eventType == null) {
				throw new IllegalStateException(String.format("Unknown type label: %s",
						predTypeLabel));
			}
			try {
				createAnnotation(jCas, trigCand.getBegin(), trigCand.getEnd(), eventType);
			} catch (UIMAException e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
	}

	private String getEventTypeLabel(JCas jCas, EventTriggerCandidate trigCand) {
		List<Event> resultList = JCasUtil.selectCovered(jCas, Event.class, trigCand);
		if (resultList.isEmpty()) {
			return null;
		}
		if (resultList.size() > 1) {
			getLogger().warn(String.format(
					"Multiple classification labels are available for trigger %s: %s. "
							+ "The first one will be chosen.",
					toPrettyString(trigCand),
					Lists.transform(resultList, getFSTypeShortName)));
		}
		return getFSTypeShortName.apply(resultList.get(0));
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

	static private final Function<FeatureStructure, String> getFSTypeShortName =
			new Function<FeatureStructure, String>() {
				@Override
				public String apply(FeatureStructure fs) {
					return fs.getType().getShortName();
				}
			};
}
