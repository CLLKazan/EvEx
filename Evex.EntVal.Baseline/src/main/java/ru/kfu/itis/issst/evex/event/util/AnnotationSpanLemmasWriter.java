/**
 * 
 */
package ru.kfu.itis.issst.evex.event.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.opencorpora.cas.Word;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ru.kfu.cll.uima.tokenizer.fstype.Token;
import ru.kfu.itis.cll.uima.io.IoUtils;
import ru.kfu.itis.issst.uima.postagger.MorphCasUtils;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class AnnotationSpanLemmasWriter extends JCasAnnotator_ImplBase {

	public static AnalysisEngineDescription createDescription(
			String annoTypeName, File outputFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(
				AnnotationSpanLemmasWriter.class,
				PARAM_OUTPUT_FILE, outputFile.getPath(),
				PARAM_ANNOTATION_TYPE, annoTypeName);
	}

	public static final String PARAM_OUTPUT_FILE = "outputFile";
	public static final String PARAM_ANNOTATION_TYPE = "annotationType";

	@ConfigurationParameter(mandatory = true, name = PARAM_OUTPUT_FILE)
	private File outputFile;
	@ConfigurationParameter(mandatory = true, name = PARAM_ANNOTATION_TYPE)
	private Class<? extends Annotation> annotationType;

	private static final Joiner lemmaJoiner = Joiner.on(' ');

	// STATE fields
	private PrintWriter out;
	private SortedSet<String> outEntries;
	// per CAS
	private Map<Token, Word> token2WordIdx;

	@Override
	public void initialize(UimaContext ctx) throws ResourceInitializationException {
		super.initialize(ctx);
		try {
			out = IoUtils.openPrintWriter(outputFile);
			outEntries = Sets.newTreeSet();
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		for (String oe : outEntries) {
			out.println(oe);
		}
		outEntries = null;
		IOUtils.closeQuietly(out);
		out = null;
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		beforeProcess(jCas);
		try {
			for (AnnotationFS anno : JCasUtil.select(jCas, annotationType)) {
				List<String> lemmas = Lists.newLinkedList();
				for (Token token : JCasUtil.selectCovered(jCas, Token.class, anno)) {
					Word w = token2WordIdx.get(token);
					if (w == null) {
						lemmas.add(token.getCoveredText());
					} else {
						lemmas.add(MorphCasUtils.getOnlyWordform(w).getLemma());
					}
				}
				outEntries.add(lemmaJoiner.join(lemmas));
			}
		} finally {
			afterProcess(jCas);
		}
	}

	private void beforeProcess(JCas jCas) {
		token2WordIdx = MorphCasUtils.getToken2WordIndex(jCas);
	}

	private void afterProcess(JCas jCas) {
		token2WordIdx = null;
	}
}
