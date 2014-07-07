/**
 * 
 */
package ru.kfu.itis.issst.evex.entval.eval;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static ru.kfu.itis.issst.evex.entval.eval.LabConstants.GRAM_MODEL_DESCRIPTOR;
import static ru.kfu.itis.issst.evex.entval.eval.LabConstants.KEY_TRAINING_DIR;

import java.io.File;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.factory.CollectionReaderFactory;

import ru.kfu.itis.cll.uima.cpe.XmiFileListReader;
import ru.kfu.itis.cll.uima.util.CorpusUtils;
import ru.kfu.itis.issst.evex.entval.SeqClassifierBasedEntityMentionDetector;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
class FeatureExtractionTask extends UimaTaskBase {

	// config
	private Class<? extends Annotation> entityTypeClass;
	// discriminators
	@Discriminator
	File corpusDir;
	@Discriminator
	int fold;

	FeatureExtractionTask(Class<? extends Annotation> entityTypeClass) {
		this.entityTypeClass = entityTypeClass;
	}

	@Override
	public CollectionReaderDescription getCollectionReaderDescription(TaskContext taskCtx)
			throws ResourceInitializationException, IOException {
		File trainingListFile = new File(corpusDir,
				CorpusUtils.getTrainPartitionFilename(fold));
		return CollectionReaderFactory.createDescription(XmiFileListReader.class,
				LabConstants.corpusTS,
				XmiFileListReader.PARAM_BASE_DIR, corpusDir.getPath(),
				XmiFileListReader.PARAM_LIST_FILE, trainingListFile.getPath());
	}

	@Override
	public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext taskCtx)
			throws ResourceInitializationException, IOException {
		File trainingDir = taskCtx.getStorageLocation(KEY_TRAINING_DIR, AccessMode.READWRITE);
		AnalysisEngineDescription featExtractorDesc = SeqClassifierBasedEntityMentionDetector
				.createFeatureExtractorDescription(
						entityTypeClass, GRAM_MODEL_DESCRIPTOR, trainingDir);
		return createAggregateDescription(featExtractorDesc);
	}

}
