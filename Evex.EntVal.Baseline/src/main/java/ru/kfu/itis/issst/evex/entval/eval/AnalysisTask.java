/**
 * 
 */
package ru.kfu.itis.issst.evex.entval.eval;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static ru.kfu.itis.issst.evex.entval.eval.LabConstants.GRAM_MODEL_DESCRIPTOR;
import static ru.kfu.itis.issst.evex.entval.eval.LabConstants.KEY_OUTPUT_DIR;
import static ru.kfu.itis.issst.evex.entval.eval.LabConstants.corpusTS;
import static ru.kfu.itis.issst.evex.entval.eval.LabConstants.modelDirKey;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

import com.google.common.collect.Lists;

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
	private Collection<Class<? extends Annotation>> entityTypeClasses;
	// disriminators
	@Discriminator
	File corpusDir;
	@Discriminator
	int fold;

	AnalysisTask(Collection<Class<? extends Annotation>> entityTypeClasses) {
		this.entityTypeClasses = entityTypeClasses;
	}

	@Override
	public CollectionReaderDescription getCollectionReaderDescription(TaskContext taskCtx)
			throws ResourceInitializationException, IOException {
		File targetFileList = new File(corpusDir, CorpusUtils.getTestPartitionFilename(fold));
		return CollectionReaderFactory.createDescription(XmiFileListReader.class,
				corpusTS,
				XmiFileListReader.PARAM_BASE_DIR, corpusDir.getPath(),
				XmiFileListReader.PARAM_LIST_FILE, targetFileList.getPath());
	}

	@Override
	public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext taskCtx)
			throws ResourceInitializationException, IOException {
		File outputDir = taskCtx.getStorageLocation(KEY_OUTPUT_DIR,
				AccessMode.READWRITE);
		List<AnalysisEngineDescription> aeDescs = Lists.newLinkedList();
		List<String> aeNames = Lists.newLinkedList();
		// 
		aeDescs.add(createGoldRemoverDesc());
		aeNames.add("goldRemover");
		//
		for (Class<? extends Annotation> entClass : entityTypeClasses) {
			File modelDir = taskCtx.getStorageLocation(modelDirKey(entClass),
					AccessMode.READONLY);
			AnalysisEngineDescription taggerDesc = SeqClassifierBasedEntityMentionDetector
					.createAnalyzerDescription(entClass, GRAM_MODEL_DESCRIPTOR, modelDir);
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
			aeDescs.add(taggerDesc);
			aeNames.add(entClass.getSimpleName() + "Tagger");
		}
		//
		aeDescs.add(createXmiWriterDesc(outputDir));
		aeNames.add("xmiWriter");
		//
		return createAggregateDescription(aeDescs, aeNames, null, null, null, null);
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
