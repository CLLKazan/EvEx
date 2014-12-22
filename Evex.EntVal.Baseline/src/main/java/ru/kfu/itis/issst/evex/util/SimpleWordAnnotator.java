/**
 * 
 */
package ru.kfu.itis.issst.evex.util;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.opencorpora.cas.Word;
import org.opencorpora.cas.Wordform;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.util.JCasUtil;

import ru.kfu.itis.issst.uima.postagger.MorphCasUtils;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class SimpleWordAnnotator extends JCasAnnotator_ImplBase {

	public static AnalysisEngineDescription createDescription()
			throws ResourceInitializationException {
		TypeSystemDescription tsd = TypeSystemDescriptionFactory.createTypeSystemDescription(
				"ru.kfu.itis.issst.evex.util.evex-util-ts");
		return AnalysisEngineFactory.createPrimitiveDescription(SimpleWordAnnotator.class, tsd);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Word srcWord : JCasUtil.select(jCas, Word.class)) {
			SimpleWord sw = new SimpleWord(jCas, srcWord.getBegin(), srcWord.getEnd());
			sw.setToken(srcWord.getToken());
			Wordform srcWf = MorphCasUtils.getOnlyWordform(srcWord);
			if (srcWf != null) {
				sw.setGrammems(srcWf.getGrammems());
				sw.setLemma(srcWf.getLemma());
			}
			sw.addToIndexes();
		}
	}

}
