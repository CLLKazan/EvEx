/**
 * 
 */
package ru.kfu.itis.issst.evex.event.eval;

import static ru.kfu.itis.cll.uima.util.CorpusUtils.getTestPartitionFilename;
import static ru.kfu.itis.issst.evex.event.util.Events.getEventTypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.kfu.itis.cll.uima.eval.EvaluationLauncher;
import ru.kfu.itis.cll.uima.eval.event.StrictPrecisionRecallListener;
import ru.kfu.itis.cll.uima.util.ConfigPropertiesUtils;
import ru.kfu.itis.cll.uima.util.Slf4jLoggerImpl;
import ru.kfu.itis.issst.evex.event.util.Events;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class EvaluateEventTriggerRecognition {

	public static void main(String[] args) throws Exception {
		Slf4jLoggerImpl.forceUsingThisImplementation();
		EvaluateEventTriggerRecognition launcher = new EvaluateEventTriggerRecognition();
		new JCommander(launcher, args);
		launcher.run();
	}

	@Parameter(required = true, names = "-g")
	private File goldCorpusDir;
	@Parameter(required = true, names = "-s")
	private File systemOutputDir;

	private Logger log = LoggerFactory.getLogger(getClass());

	private void run() throws Exception {
		Properties evalCfg = readEvaluationConfig();
		//
		Map<String, String> phValues = Maps.newHashMap();
		// inject outputBaseDir
		phValues.put("outputBaseDir", systemOutputDir.getPath());
		// inject annotation types
		phValues.put("annotationTypes", annoTypeNameJoiner.join(getEventTypes()));
		ConfigPropertiesUtils.replacePlaceholders(evalCfg, phValues);
		// setup paths to a system output dir and a gold corpus
		evalCfg.setProperty("goldCasDirectory.dir", goldCorpusDir.getPath());
		evalCfg.setProperty("goldCasDirectory.listFile",
				new File(goldCorpusDir, getTestPartitionFilename(0)).getPath());
		evalCfg.setProperty("systemCasDirectory.dir", systemOutputDir.getPath());
		// add listeners
		for (String evType : getEventTypes()) {
			setupEvalListener(evType, evalCfg);
		}
		// add matchers
		for (String evType : getEventTypes()) {
			setupMatcher(evType, evalCfg);
		}
		if (log.isInfoEnabled()) {
			log.info("Evaluation config:\n {}",
					ConfigPropertiesUtils.prettyString(evalCfg));
		}
		EvaluationLauncher.runUsingProperties(evalCfg);
	}

	private void setupEvalListener(final String eventTypeName, final Properties cfg) {
		final String eventTypeShort = Events.getTypeShortName(eventTypeName);
		String listenerName = String.format("listener.%sEval", eventTypeShort);
		String listenerPropertyPrefix = String.format("listenerProperty.%sEval", eventTypeShort);
		cfg.setProperty(listenerName, EVAL_LISTENER_CLASS_NAME);
		cfg.setProperty(listenerPropertyPrefix + ".targetTypeName", eventTypeName);
		cfg.setProperty(listenerPropertyPrefix + ".outputFile",
				new File(systemOutputDir, eventTypeShort + "-eval-results.txt").getPath());
	}

	private void setupMatcher(final String eventTypeName, final Properties cfg) {
		final String eventTypeShort = Events.getTypeShortName(eventTypeName);
		cfg.setProperty("check." + eventTypeShort, "checkBoundaries");
	}

	static private final String EVAL_LISTENER_CLASS_NAME =
			StrictPrecisionRecallListener.class.getName();
	static private final Joiner annoTypeNameJoiner = Joiner.on(',');

	private Properties readEvaluationConfig() throws IOException {
		Properties evalProps = new Properties();
		String evalPropsPath = "ru/kfu/itis/issst/evex/event/eval/eval.properties.template";
		InputStream evalPropsIS = getClassLoader().getResourceAsStream(evalPropsPath);
		if (evalPropsIS == null) {
			throw new IllegalStateException(String.format(
					"Can't find classpath resource %s",
					evalPropsPath));
		}
		Reader evalPropsReader = new BufferedReader(new InputStreamReader(evalPropsIS, "utf-8"));
		try {
			evalProps.load(evalPropsReader);
		} finally {
			evalPropsReader.close();
		}
		return evalProps;
	}

	private ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
}
