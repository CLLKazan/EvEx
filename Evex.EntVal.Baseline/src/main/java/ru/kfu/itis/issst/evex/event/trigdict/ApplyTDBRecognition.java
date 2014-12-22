/**
 * 
 */
package ru.kfu.itis.issst.evex.event.trigdict;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createDescription;
import static org.uimafit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.metadata.Import;
import org.apache.uima.resource.metadata.MetaDataObject;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.resource.metadata.impl.Import_impl;
import org.uimafit.pipeline.SimplePipeline;

import ru.kfu.itis.cll.uima.annotator.AnnotationRemover;
import ru.kfu.itis.cll.uima.consumer.XmiWriter;
import ru.kfu.itis.cll.uima.cpe.XmiFileListReader;
import ru.kfu.itis.cll.uima.util.CorpusUtils;
import ru.kfu.itis.cll.uima.util.PipelineDescriptorUtils;
import ru.kfu.itis.cll.uima.util.Slf4jLoggerImpl;
import ru.kfu.itis.issst.evex.entval.EntvalRecognizerAPI;
import ru.kfu.itis.issst.evex.event.util.Events;
import ru.kfu.itis.issst.evex.util.SimpleWordAnnotator;
import ru.kfu.itis.issst.uima.postagger.PosTaggerAPI;
import ru.kfu.itis.issst.uima.segmentation.SentenceSplitterAPI;
import ru.kfu.itis.issst.uima.tokenizer.TokenizerAPI;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.Maps;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class ApplyTDBRecognition {

	public static void main(String[] args) throws UIMAException, IOException {
		Slf4jLoggerImpl.forceUsingThisImplementation();
		ApplyTDBRecognition launcher = new ApplyTDBRecognition();
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
		CollectionReaderDescription colReaderDesc = createDescription(XmiFileListReader.class,
				inputTSD,
				XmiFileListReader.PARAM_BASE_DIR, corpusDir.getPath(),
				XmiFileListReader.PARAM_LIST_FILE,
				new File(corpusDir, CorpusUtils.getTestPartitionFilename(0)));
		//
		Map<String, MetaDataObject> pipelineAEDescs = Maps.newLinkedHashMap();
		//
		pipelineAEDescs
				.put("gold-annotations-remover",
						createPrimitiveDescription(AnnotationRemover.class,
								AnnotationRemover.PARAM_TYPES_TO_REMOVE,
								Events.getEventTypes()));
		pipelineAEDescs.put("simple-word-annotator", SimpleWordAnnotator.createDescription());
		//
		{
			Import tdbReconizerImport = new Import_impl();
			tdbReconizerImport
					.setName("ru.kfu.itis.issst.evex.event.TriggerDictionaryBasedETRecognizer");
			pipelineAEDescs.put("trid-dict-recognizer", tdbReconizerImport);
		}
		//
		pipelineAEDescs
				.put("util-annotations-remover",
						createPrimitiveDescription(AnnotationRemover.class,
								AnnotationRemover.PARAM_NAMESPACES_TO_REMOVE,
								new String[] { "ru.kfu.itis.issst.evex.util" }));
		pipelineAEDescs.put("xmiWriter", XmiWriter.createDescription(outputDir, true));
		//
		SimplePipeline.runPipeline(colReaderDesc,
				PipelineDescriptorUtils.createAggregateDescription(pipelineAEDescs));
	}
}
