/**
 * 
 */
package ru.kfu.itis.issst.evex.entval.eval;

import java.util.List;

import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.TypeSystemDescriptionFactory;

import com.google.common.collect.ImmutableList;

import ru.kfu.itis.issst.evex.Facility;
import ru.kfu.itis.issst.evex.GPE;
import ru.kfu.itis.issst.evex.Location;
import ru.kfu.itis.issst.evex.Money;
import ru.kfu.itis.issst.evex.Organization;
import ru.kfu.itis.issst.evex.Person;
import ru.kfu.itis.issst.evex.Time;
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

	public static final List<Class<? extends Annotation>> ENTVAL_CLASSES = ImmutableList.of(
			Person.class,
			Organization.class,
			// GPE.class,
			Facility.class,
			Location.class,
			Time.class,
			Money.class);

	public static String modelDirKey(Class<? extends Annotation> entvalClass) {
		return KEY_MODEL_DIR + "-" + entvalClass.getSimpleName();
	}
}
