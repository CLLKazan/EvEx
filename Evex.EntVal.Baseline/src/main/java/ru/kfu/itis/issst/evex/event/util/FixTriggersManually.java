/**
 * 
 */
package ru.kfu.itis.issst.evex.event.util;

import static org.uimafit.factory.CollectionReaderFactory.createDescription;
import static org.uimafit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;

import ru.kfu.itis.cll.uima.consumer.XmiWriter;
import ru.kfu.itis.cll.uima.cpe.XmiCollectionReader;
import ru.kfu.itis.cll.uima.util.Slf4jLoggerImpl;
import ru.kfu.itis.issst.evex.entval.EntvalRecognizerAPI;
import ru.kfu.itis.issst.uima.postagger.PosTaggerAPI;
import ru.kfu.itis.issst.uima.segmentation.SentenceSplitterAPI;
import ru.kfu.itis.issst.uima.tokenizer.TokenizerAPI;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.Lists;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class FixTriggersManually {

	public static void main(String[] args) throws UIMAException, IOException {
		Slf4jLoggerImpl.forceUsingThisImplementation();
		FixTriggersManually launcher = new FixTriggersManually();
		new JCommander(launcher, args);
		launcher.run();
	}

	@Parameter(names = { "-c" }, required = true)
	private File corpusDir;

	@Parameter(names = { "--output-dir", "-o" }, required = true)
	private File outputDir;

	private final TypeSystemDescription inputTSD = createTypeSystemDescription(
			"ru.kfu.itis.cll.uima.commons.Commons-TypeSystem",
			TokenizerAPI.TYPESYSTEM_TOKENIZER,
			SentenceSplitterAPI.TYPESYSTEM_SENTENCES,
			PosTaggerAPI.TYPESYSTEM_POSTAGGER,
			EntvalRecognizerAPI.TYPESYSTEM_ENTVAL);

	private void run() throws UIMAException, IOException {
		CollectionReaderDescription colReaderDesc = createDescription(XmiCollectionReader.class,
				inputTSD,
				XmiCollectionReader.PARAM_INPUTDIR, corpusDir.getPath());
		List<AnalysisEngineDescription> pipelineAEDescs = Lists.newLinkedList();
		List<String> pipelineAENames = Lists.newLinkedList();
		//
		pipelineAEDescs.add(InteractiveTriggerFixer.createDescription(
				new File(outputDir, "change-log.txt")));
		pipelineAENames.add("interactive-trigger-fixer");
		//
		pipelineAEDescs.add(XmiWriter.createDescription(outputDir, true));
		pipelineAENames.add("xmiWriter");
		//
		SimplePipeline.runPipeline(colReaderDesc, AnalysisEngineFactory.createAggregateDescription(
				pipelineAEDescs, pipelineAENames, null, null, null, null));
	}
}
