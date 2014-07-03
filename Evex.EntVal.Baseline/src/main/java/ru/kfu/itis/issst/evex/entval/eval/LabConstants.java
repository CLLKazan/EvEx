/**
 * 
 */
package ru.kfu.itis.issst.evex.entval.eval;

import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.TypeSystemDescriptionFactory;

import ru.kfu.itis.issst.evex.entval.EntvalRecognizerAPI;
import ru.kfu.itis.issst.uima.segmentation.SentenceSplitterAPI;
import ru.kfu.itis.issst.uima.tokenizer.TokenizerAPI;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class LabConstants {

	public static final TypeSystemDescription corpusTS = TypeSystemDescriptionFactory
			.createTypeSystemDescription(
					"ru.kfu.itis.cll.uima.commons.Commons-TypeSystem",
					TokenizerAPI.TYPESYSTEM_TOKENIZER,
					SentenceSplitterAPI.TYPESYSTEM_SENTENCES,
					"org.opencorpora.morphology-ts",
					EntvalRecognizerAPI.TYPESYSTEM_ENTVAL
			);

	public static final String KEY_TRAINING_DIR = "TrainingDir";
	public static final String KEY_MODEL_DIR = "ModelDir";
	public static final String KEY_OUTPUT_DIR = "OutputDir";
	// task discriminator names
	public static final String DISCRIMINATOR_FOLD = "fold";
	public static final String DISCRIMINATOR_CORPUS_DIR = "corpusDir";
}
