/**
 * 
 */
package ru.kfu.itis.issst.evex.entval.eval;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ConfigurationParameterFactory;

import ru.kfu.itis.cll.uima.annotator.AnnotationRemover;
import ru.kfu.itis.cll.uima.consumer.XmiWriter;
import ru.kfu.itis.cll.uima.cpe.XmiFileListReader;
import ru.kfu.itis.cll.uima.util.CorpusUtils;
import ru.kfu.itis.issst.cleartk.GenericJarClassifierFactory;
import ru.kfu.itis.issst.evex.entval.SeqClassifierBasedEntityMentionDetector;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
class AnalysisTask extends UimaTaskBase {

	// config
	private Class<? extends Annotation> entityTypeClass;
	// disriminators
	@Discriminator
	File corpusDir;
	@Discriminator
	int fold;

	AnalysisTask(Class<? extends Annotation> entityTypeClass) {
		this.entityTypeClass = entityTypeClass;
	}

	@Override
	public CollectionReaderDescription getCollectionReaderDescription(TaskContext taskCtx)
			throws ResourceInitializationException, IOException {
		File targetFileList = new File(corpusDir, CorpusUtils.getTestPartitionFilename(fold));
		return CollectionReaderFactory.createDescription(XmiFileListReader.class,
				LabConstants.corpusTS,
				XmiFileListReader.PARAM_BASE_DIR, corpusDir.getPath(),
				XmiFileListReader.PARAM_LIST_FILE, targetFileList.getPath());
	}

	@Override
	public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext taskCtx)
			throws ResourceInitializationException, IOException {
		File modelDir = taskCtx.getStorageLocation(LabConstants.KEY_MODEL_DIR,
				AccessMode.READONLY);
		File outputDir = taskCtx.getStorageLocation(LabConstants.KEY_OUTPUT_DIR,
				AccessMode.READWRITE);
		// 
		AnalysisEngineDescription goldRemoverDesc = createGoldRemoverDesc();
		//
		AnalysisEngineDescription taggerDesc = SeqClassifierBasedEntityMentionDetector
				.createAnalyzerDescription(entityTypeClass, modelDir);
		// We should specify additional paths to resolve relative paths of model jars.  
		// There are several ways to do this. E.g., we can change global UIMA data-path.
		// But the better solution is to provide the parameter for JarClassifierFactory.
		final String addSearchPathParam = GenericJarClassifierFactory.PARAM_ADDITIONAL_SEARCH_PATHS;
		ConfigurationParameterFactory.addConfigurationParameter(taggerDesc, addSearchPathParam,
				new String[] { modelDir.getPath() });
		// disable multiple deployment to avoid heavy memory consumption and related consequences 
		taggerDesc.getAnalysisEngineMetaData().getOperationalProperties()
				.setMultipleDeploymentAllowed(false);
		//
		AnalysisEngineDescription xmiWriterDesc = createXmiWriterDesc(outputDir);
		//
		return createAggregateDescription(goldRemoverDesc, taggerDesc, xmiWriterDesc);
	}

	private AnalysisEngineDescription createGoldRemoverDesc()
			throws ResourceInitializationException {
		return createPrimitiveDescription(
				AnnotationRemover.class,
				AnnotationRemover.PARAM_NAMESPACES_TO_REMOVE,
				Arrays.asList("ru.kfu.itis.issst.evex"));
	}

	private AnalysisEngineDescription createXmiWriterDesc(File outputDir)
			throws ResourceInitializationException {
		return XmiWriter.createDescription(outputDir, true);
	}
}
