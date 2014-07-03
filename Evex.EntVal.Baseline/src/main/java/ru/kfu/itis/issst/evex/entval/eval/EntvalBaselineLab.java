/**
 * 
 */
package ru.kfu.itis.issst.evex.entval.eval;

import static ru.kfu.itis.issst.evex.entval.eval.LabConstants.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Splitter;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import ru.kfu.itis.cll.uima.io.IoUtils;
import ru.kfu.itis.cll.uima.util.CorpusUtils;
import ru.kfu.itis.cll.uima.util.Slf4jLoggerImpl;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class EntvalBaselineLab {

	public static void main(String[] args) throws Exception {
		EntvalBaselineLab lab = new EntvalBaselineLab();
		new JCommander(lab, args);
		if (lab.wrkDirArgs.size() > 1) {
			System.err.println("Specify only one working dir!");
			System.exit(1);
		}
		System.setProperty("DKPRO_HOME", lab.wrkDirArgs.get(0).getPath());
		lab.run();
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Parameter(names = "--parameters-file", required = false)
	private File parametersFile = new File(getClass().getSimpleName() + ".parameters");

	@Parameter(required = true, description = "working directory")
	private List<File> wrkDirArgs;

	private void run() throws Exception {
		final File corpusDir = getOnlyFileVal(DISCRIMINATOR_CORPUS_DIR);
		// read fold num from the corpus dir
		Integer foldNum = 0;
		{
			while (true) {
				String tpFilename = CorpusUtils.getTrainPartitionFilename(foldNum);
				File tpFile = new File(corpusDir, tpFilename);
				if (tpFile.isFile()) {
					foldNum++;
				} else {
					break;
				}
			}
		}
		log.info("Cross-validation folds for the corpus: {}", foldNum);
		//
		UimaTaskBase featureExtractionTask = new FeatureExtractionTask();
		featureExtractionTask.setType("FeatureExtraction");
		// -----------------------------------------------------------------
		ExecutableTaskBase trainingTask = new TrainingTask();
		trainingTask.setType("Training");
		// -----------------------------------------------------------------
		UimaTaskBase analysisTask = new AnalysisTask();
		analysisTask.setType("Analysis");
		// -----------------------------------------------------------------
		ExecutableTaskBase evaluationTask = new EvaluationTask();
		evaluationTask.setType("Evaluation");
		// -----------------------------------------------------------------
		// configure data-flow between tasks
		trainingTask.addImport(featureExtractionTask, KEY_TRAINING_DIR);
		analysisTask.addImport(trainingTask, KEY_MODEL_DIR);
		evaluationTask.addImport(analysisTask, KEY_OUTPUT_DIR);
		// -----------------------------------------------------------------
		// create parameter space
		ContiguousSet<Integer> foldSet = ContiguousSet.create(Range.closedOpen(0, foldNum),
				DiscreteDomain.integers());
		ParameterSpace pSpace = new ParameterSpace(
				Dimension.create(DISCRIMINATOR_CORPUS_DIR, corpusDir),
				Dimension.create(DISCRIMINATOR_FOLD, foldSet.toArray(new Integer[foldSet.size()])),
				getIntDimension("featureMinFreq"),
				getIntDimension("c2"),
				getBoolDimension("featurePossibleTransitions"),
				getBoolDimension("featurePossibleStates"),
				getIntDimension("optMaxIterations"));
		// -----------------------------------------------------------------
		// create and run BatchTask
		BatchTask batchTask = new BatchTask();
		batchTask.addTask(featureExtractionTask);
		batchTask.addTask(trainingTask);
		batchTask.addTask(analysisTask);
		batchTask.addTask(evaluationTask);
		// 
		batchTask.setParameterSpace(pSpace);
		batchTask.setExecutionPolicy(ExecutionPolicy.USE_EXISTING);
		Lab.getInstance().run(batchTask);
	}

	// XXX
	// TODO use from UIMA.Ext.Lab-Commons
	// XXX
	private Properties parameterProps;

	private List<String> getParamVals(String paramName) throws IOException {
		if (parameterProps == null) {
			parameterProps = IoUtils.readProperties(parametersFile);
		}
		String str = parameterProps.getProperty(paramName);
		if (str == null) {
			return Lists.newArrayList((String) null);
		}
		return Lists.newArrayList(paramValSplitter.split(str));
	}

	// -------------------
	private List<File> getFileVals(String paramName) throws IOException {
		return getParamObjects(paramName, File.class, str2File);
	}

	private File getOnlyFileVal(String paramName) throws IOException {
		List<File> files = getFileVals(paramName);
		if (files.size() != 1) {
			throw new IllegalStateException(String.format(
					"Single value is expected for parameter %s. Was:\n%s",
					paramName, files));
		}
		return files.get(0);
	}

	private <V> List<V> getParamObjects(String paramName, Class<V> valClass,
			Function<String, V> converter) throws IOException {
		return Lists.transform(getParamVals(paramName), converter);
	}

	// XXX
	// TODO use from UIMA.Ext.Lab-Commons
	// XXX
	private static final Splitter paramValSplitter = Splitter.on(';').trimResults();
	private static final Splitter collectionValSplitter = Splitter.on(',').trimResults();

	protected Dimension<File> getFileDimension(String paramName) throws IOException {
		return getDimension(paramName, File.class, str2File);
	}

	protected Dimension<Integer> getIntDimension(String paramName) throws IOException {
		return getDimension(paramName, Integer.class, str2Int);
	}

	protected Dimension<Boolean> getBoolDimension(String paramName) throws IOException {
		return getDimension(paramName, Boolean.class, str2Boolean);
	}

	protected Dimension<String> getStringDimension(String paramName) throws IOException {
		return getDimension(paramName, String.class, Functions.<String> identity());
	}

	protected Dimension<List<String>> getStringListDimension(String paramName) throws IOException {
		List<String> paramValStrs = getParamVals(paramName);
		List<List<String>> paramVals = Lists.transform(paramValStrs, str2StringList);
		return Dimension.create(paramName, toArrayOfLists(paramVals));
	}

	protected Dimension<Set<String>> getStringSetDimension(String paramName) throws IOException {
		List<String> paramValStrs = getParamVals(paramName);
		List<Set<String>> paramVals = Lists.transform(paramValStrs, str2StringSet);
		return Dimension.create(paramName, toArrayOfSets(paramVals));
	}

	@SuppressWarnings("unchecked")
	private static <V> List<V>[] toArrayOfLists(List<List<V>> list) {
		return list.toArray(new List[list.size()]);
	}

	@SuppressWarnings("unchecked")
	private static <V> Set<V>[] toArrayOfSets(List<Set<V>> list) {
		return list.toArray(new Set[list.size()]);
	}

	private <V> Dimension<V> getDimension(String paramName, Class<V> valClass,
			Function<String, V> converter) throws IOException {
		return toDimension(paramName, Lists.transform(getParamVals(paramName), converter), valClass);
	}

	@SuppressWarnings("unchecked")
	private static <V> Dimension<V> toDimension(String name, List<V> list, Class<V> valClass) {
		return Dimension.create(name, list.toArray(
				(V[]) Array.newInstance(valClass, list.size())));
	}

	private static final Function<String, File> str2File = new Function<String, File>() {
		@Override
		public File apply(String input) {
			if (input == null) {
				return null;
			}
			if ("null".equalsIgnoreCase(input)) {
				return null;
			}
			return new File(input);
		}
	};

	private static final Function<String, Integer> str2Int = new Function<String, Integer>() {
		@Override
		public Integer apply(String input) {
			if (input == null) {
				return null;
			}
			return Integer.valueOf(input);
		}
	};

	private static final Function<String, Boolean> str2Boolean = new Function<String, Boolean>() {
		@Override
		public Boolean apply(String input) {
			if (input == null) {
				return null;
			}
			if ("0".equals(input)) {
				return false;
			}
			if ("1".equals(input)) {
				return true;
			}
			return Boolean.valueOf(input);
		}
	};

	private static final Function<String, List<String>> str2StringList = new Function<String, List<String>>() {
		@Override
		public List<String> apply(String input) {
			if (input == null) {
				return null;
			}
			return Lists.newArrayList(collectionValSplitter.split(input));
		}
	};

	private static final Function<String, Set<String>> str2StringSet = new Function<String, Set<String>>() {
		@Override
		public Set<String> apply(String input) {
			if (input == null) {
				return null;
			}
			return Sets.newLinkedHashSet(collectionValSplitter.split(input));
		}
	};

	static {
		Slf4jLoggerImpl.forceUsingThisImplementation();
	}
}
