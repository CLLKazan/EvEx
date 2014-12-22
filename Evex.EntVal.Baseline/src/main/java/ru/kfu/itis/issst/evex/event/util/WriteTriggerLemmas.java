/**
 * 
 */
package ru.kfu.itis.issst.evex.event.util;

import static org.uimafit.factory.CollectionReaderFactory.createDescription;
import static org.uimafit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ru.kfu.itis.cll.uima.cpe.XmiCollectionReader;
import ru.kfu.itis.cll.uima.util.Slf4jLoggerImpl;
import ru.kfu.itis.issst.evex.Event;
import ru.kfu.itis.issst.evex.entval.EntvalRecognizerAPI;
import ru.kfu.itis.issst.uima.postagger.PosTaggerAPI;
import ru.kfu.itis.issst.uima.segmentation.SentenceSplitterAPI;
import ru.kfu.itis.issst.uima.tokenizer.TokenizerAPI;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class WriteTriggerLemmas {

	public static void main(String[] args) throws UIMAException, IOException {
		Slf4jLoggerImpl.forceUsingThisImplementation();
		WriteTriggerLemmas launcher = new WriteTriggerLemmas();
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
		Set<String> eventTypeNames = collectEventTypes();
		CollectionReaderDescription colReaderDesc = createDescription(XmiCollectionReader.class,
				inputTSD,
				XmiCollectionReader.PARAM_INPUTDIR, corpusDir.getPath());
		List<AnalysisEngineDescription> pipelineAEDescs = Lists.newLinkedList();
		List<String> pipelineAENames = Lists.newLinkedList();
		for (String etn : eventTypeNames) {
			pipelineAENames.add(etn + "-lemma-writer");
			pipelineAEDescs.add(AnnotationSpanLemmasWriter.createDescription(etn,
					new File(outputDir, getOutputFileName(etn))));
		}
		SimplePipeline.runPipeline(colReaderDesc, AnalysisEngineFactory.createAggregateDescription(
				pipelineAEDescs, pipelineAENames, null, null, null, null));
	}

	private String getOutputFileName(String annoTypeName) {
		int lastDotIdx = annoTypeName.lastIndexOf('.');
		if (lastDotIdx >= 0) {
			return annoTypeName.substring(lastDotIdx + 1);
		} else {
			return annoTypeName;
		}
	}

	private Set<String> collectEventTypes() throws UIMAException {
		TypeSystem ts = CasCreationUtils.createCas(inputTSD, null, null).getTypeSystem();
		Type eventSuperType = ts.getType(Event.class.getName());
		List<Type> eventTypes = ts.getDirectSubtypes(eventSuperType);
		return Sets.newHashSet(Lists.transform(eventTypes, new Function<Type, String>() {
			@Override
			public String apply(Type input) {
				return input.getName();
			}
		}));
	}
}
