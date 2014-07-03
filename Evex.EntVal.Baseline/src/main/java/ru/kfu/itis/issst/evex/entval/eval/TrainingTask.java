/**
 * 
 */
package ru.kfu.itis.issst.evex.entval.eval;

import static ru.kfu.itis.issst.evex.entval.eval.LabConstants.KEY_MODEL_DIR;
import static ru.kfu.itis.issst.evex.entval.eval.LabConstants.KEY_TRAINING_DIR;

import java.io.File;
import java.util.List;

import ru.kfu.itis.issst.evex.entval.TrainingTool;

import com.google.common.collect.Lists;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
class TrainingTask extends ExecutableTaskBase {
	@Discriminator
	int featureMinFreq;
	@Discriminator
	boolean featurePossibleStates;
	@Discriminator
	boolean featurePossibleTransitions;
	@Discriminator
	int c2;
	@Discriminator
	int optMaxIterations;

	@Override
	public void execute(TaskContext taskCtx) throws Exception {
		File trainingBaseDir = taskCtx.getStorageLocation(KEY_TRAINING_DIR,
				AccessMode.READONLY);
		File modelBaseDir = taskCtx.getStorageLocation(KEY_MODEL_DIR, AccessMode.READWRITE);
		//
		// set training parameters
		List<String> trainerArgs = Lists.newArrayList();
		trainerArgs.add("-a");
		trainerArgs.add("lbfgs");
		addTrainParam(trainerArgs, "max_iterations", optMaxIterations);
		addTrainParam(trainerArgs, "feature.minfreq", featureMinFreq);
		if (featurePossibleStates) {
			addTrainParam(trainerArgs, "feature.possible_states", 1);
		}
		if (featurePossibleTransitions) {
			addTrainParam(trainerArgs, "feature.possible_transitions", 1);
		}
		addTrainParam(trainerArgs, "c2", c2);
		//
		TrainingTool.trainModels(trainingBaseDir, modelBaseDir,
				trainerArgs.toArray(new String[trainerArgs.size()]));
	}

	// TODO:LOW move to utils package of crfsute4j
	private static void addTrainParam(List<String> params, String name, int value) {
		params.add("-p");
		params.add(name + "=" + value);
	}
}
