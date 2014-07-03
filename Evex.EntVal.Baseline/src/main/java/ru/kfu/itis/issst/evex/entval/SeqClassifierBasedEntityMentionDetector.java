/**
 * 
 */
package ru.kfu.itis.issst.evex.entval;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.io.File;
import java.net.URI;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;

import ru.kfu.itis.issst.cleartk.JarSequenceClassifierFactory;
import ru.kfu.itis.issst.cleartk.crfsuite.CRFSuiteStringOutcomeDataWriterFactory;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class SeqClassifierBasedEntityMentionDetector extends CleartkSequenceAnnotator<String> {

	public static AnalysisEngineDescription createFeatureExtractorDescription(File trainingDir)
			throws ResourceInitializationException {
		return createPrimitiveDescription(SeqClassifierBasedEntityMentionDetector.class,
				PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
				CRFSuiteStringOutcomeDataWriterFactory.class.getName(),
				DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, trainingDir);
	}

	public static AnalysisEngineDescription createAnalyzerDescription(File modelDir)
			throws ResourceInitializationException {
		File modelJarFile = JarClassifierBuilder.getModelJarFile(modelDir);
		// make model jar path relative to modelBaseDir
		String jarRelativePath = relativize(modelDir, modelJarFile);
		return createPrimitiveDescription(SeqClassifierBasedEntityMentionDetector.class,
				PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
				JarSequenceClassifierFactory.class.getName(),
				JarSequenceClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				jarRelativePath);
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

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub

	}

}
