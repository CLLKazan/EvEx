/**
 * 
 */
package ru.kfu.itis.issst.evex.entval;

import java.io.File;

import org.cleartk.classifier.jar.JarClassifierBuilder;

import ru.kfu.itis.issst.cleartk.crfsuite.CRFSuiteStringOutcomeClassifierBuilder;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class TrainingTool {

	public static void trainModels(File trainingDir, File modelDir, String[] trainerArgs)
			throws Exception {
		// TODO The following lines contain a few hacks to avoid
		// extensive training file duplicates reproduction
		JarClassifierBuilder<?> _classifierBuilder = JarClassifierBuilder
				.fromTrainingDirectory(trainingDir);
		CRFSuiteStringOutcomeClassifierBuilder classifierBuilder =
				(CRFSuiteStringOutcomeClassifierBuilder) _classifierBuilder;
		// invoke implementation-specific method (i.e., it is not declared in the interface)
		classifierBuilder.trainClassifier(modelDir, trainingDir, trainerArgs);
		classifierBuilder.packageClassifier(modelDir);
	}

	private TrainingTool() {
	}
}
