/**
 * 
 */
package ru.kfu.itis.issst.evex.entval.corpus;

import static ru.kfu.itis.cll.uima.cas.AnnotationUtils.getOverlapping;
import static ru.kfu.itis.cll.uima.cas.AnnotationUtils.overlap;
import static ru.kfu.itis.cll.uima.cas.AnnotationUtils.toPrettyString;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import ru.kfu.cll.uima.tokenizer.fstype.Token;
import ru.kfu.itis.cll.uima.util.Offsets;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * TODO test
 * 
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class AnnotationToTokenFitter<ST extends Annotation> extends JCasAnnotator_ImplBase {

	public static AnalysisEngineDescription createDescription(
			Class<? extends Annotation> targetTypeClass) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(AnnotationToTokenFitter.class,
				PARAM_TARGET_TYPE, targetTypeClass.getName());
	}

	public static final String PARAM_TARGET_TYPE = "targetType";

	@ConfigurationParameter(name = PARAM_TARGET_TYPE, mandatory = true)
	private Class<ST> targetTypeClass;

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		Set<ST> anno2Remove = Sets.newLinkedHashSet();
		Map<ST, Offsets> anno2Fix = Maps.newLinkedHashMap();
		//
		for (ST span : JCasUtil.select(jCas, targetTypeClass)) {
			Offsets spanOffsets = new Offsets(span);
			//
			List<Token> spanTokens = JCasUtil.selectCovered(Token.class, span);
			if (spanTokens.isEmpty()) {
				// search for overlapping
				FSIterator<Annotation> overlappingTokens = getOverlapping(jCas.getCas(),
						jCas.getAnnotationIndex(Token.type).iterator(), span);
				if (!overlappingTokens.hasNext()) {
					getLogger().warn(String.format(
							"No tokens for typed span: %s",
							toPrettyString(span)));
					anno2Remove.add(span);
				} else {
					spanOffsets = new Offsets(overlappingTokens.next());
					while (overlappingTokens.hasNext()) {
						spanOffsets = new Offsets(
								spanOffsets.getBegin(),
								overlappingTokens.next().getEnd());
					}
				}
			} else {
				// span has tokens
				if (spanTokens.get(0).getBegin() != span.getBegin()) {
					List<Token> precTokens = JCasUtil.selectPreceding(jCas, Token.class, span, 1);
					if (precTokens.size() > 1) {
						// should never happen
						throw new IllegalStateException();
					}
					Token precToken = precTokens.isEmpty() ? null : precTokens.get(0);
					if (precToken != null && overlap(span, precToken)) {
						// fix begin of the span
						spanOffsets = new Offsets(precToken.getBegin(), spanOffsets.getEnd());
					}
				}
				//
				if (spanTokens.get(spanTokens.size() - 1).getEnd() != span.getEnd()) {
					List<Token> folTokens = JCasUtil.selectFollowing(jCas, Token.class, span, 1);
					if (folTokens.size() > 1) {
						// should never happen
						throw new IllegalStateException();
					}
					Token folToken = folTokens.isEmpty() ? null : folTokens.get(0);
					if (folToken != null && overlap(span, folToken)) {
						// fix end of the span
						spanOffsets = new Offsets(spanOffsets.getBegin(), folToken.getEnd());
					}
				}
			}
			//
			if (!spanOffsets.isIdenticalWith(span)) {
				// fix
				anno2Fix.put(span, spanOffsets);
			}
		}
		// remove empty spans
		for (ST span : anno2Remove) {
			span.removeFromIndexes();
		}
		// fix span offsets
		for (Map.Entry<ST, Offsets> e : anno2Fix.entrySet()) {
			ST span = e.getKey();
			String oldText = span.getCoveredText();
			Offsets fixedOffsets = e.getValue();
			span.removeFromIndexes();
			span.setBegin(fixedOffsets.getBegin());
			span.setEnd(fixedOffsets.getEnd());
			span.addToIndexes();
			String newText = span.getCoveredText();
			getLogger().debug(String.format(
					"Annotation offsets are fixed: '%s' => '%s'",
					oldText, newText));
		}
	}
}
