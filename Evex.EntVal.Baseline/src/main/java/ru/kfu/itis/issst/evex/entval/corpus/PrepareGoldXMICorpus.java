/**
 * 
 */
package ru.kfu.itis.issst.evex.entval.corpus;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createDescription;
import static org.uimafit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;
import static ru.kfu.itis.cll.uima.annotator.FeatureValueReplacer.PARAM_ANNO_TYPE;
import static ru.kfu.itis.cll.uima.annotator.FeatureValueReplacer.PARAM_FEATURE_PATH;
import static ru.kfu.itis.cll.uima.annotator.FeatureValueReplacer.PARAM_PATTERN;
import static ru.kfu.itis.cll.uima.annotator.FeatureValueReplacer.PARAM_REPLACE_BY;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.pipeline.SimplePipeline;

import ru.kfu.itis.cll.uima.annotator.FeatureValueReplacer;
import ru.kfu.itis.cll.uima.annotator.NestedAnnotationRemover;
import ru.kfu.itis.cll.uima.commons.DocumentMetadata;
import ru.kfu.itis.cll.uima.consumer.XmiWriter;
import ru.kfu.itis.cll.uima.cpe.XmiCollectionReader;
import ru.kfu.itis.cll.uima.util.Slf4jLoggerImpl;
import ru.kfu.itis.issst.evex.Entity;
import ru.kfu.itis.issst.evex.Facility;
import ru.kfu.itis.issst.evex.GPEGPE;
import ru.kfu.itis.issst.evex.GPELocation;
import ru.kfu.itis.issst.evex.GPEOrganization;
import ru.kfu.itis.issst.evex.GPEPerson;
import ru.kfu.itis.issst.evex.Location;
import ru.kfu.itis.issst.evex.Money;
import ru.kfu.itis.issst.evex.Organization;
import ru.kfu.itis.issst.evex.Person;
import ru.kfu.itis.issst.evex.Time;
import ru.kfu.itis.issst.evex.Value;
import ru.kfu.itis.issst.evex.entval.EntvalRecognizerAPI;
import ru.kfu.itis.issst.uima.segmentation.SentenceSplitterAPI;
import ru.kfu.itis.issst.uima.tokenizer.TokenizerAPI;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class PrepareGoldXMICorpus {

	public static void main(String[] args) throws UIMAException, IOException {
		Slf4jLoggerImpl.forceUsingThisImplementation();
		PrepareGoldXMICorpus obj = new PrepareGoldXMICorpus();
		new JCommander(obj, args);
		obj.run();
	}

	private static final List<String> ENTVAL_TYPE_NAMES = ImmutableList.of(
			Person.class.getName(),
			Organization.class.getName(),
			GPEOrganization.class.getName(),
			GPELocation.class.getName(),
			GPEGPE.class.getName(),
			GPEPerson.class.getName(),
			Location.class.getName(),
			Facility.class.getName(),
			Time.class.getName(),
			Money.class.getName());

	@Parameter(names = { "-c" }, required = true)
	private File corpusDir;

	@Parameter(names = { "--output-dir", "-o" }, required = true)
	private File outputDir;

	private final TypeSystemDescription inputTSD = createTypeSystemDescription(
			"ru.kfu.itis.cll.uima.commons.Commons-TypeSystem",
			EntvalRecognizerAPI.TYPESYSTEM_ENTVAL);

	private PrepareGoldXMICorpus() {
	}

	private void run() throws UIMAException, IOException {
		CollectionReaderDescription colReaderDesc = createDescription(XmiCollectionReader.class,
				inputTSD,
				XmiCollectionReader.PARAM_INPUTDIR, corpusDir.getPath());

		List<AnalysisEngineDescription> pipeline = Lists.newLinkedList();
		pipeline.add(TokenizerAPI.getAEDescription());
		pipeline.add(SentenceSplitterAPI.getAEDescription());
		//
		pipeline.add(AnnotationToTokenFitter.createDescription(Entity.class));
		pipeline.add(AnnotationToTokenFitter.createDescription(Value.class));
		//
		for (String annoType : ENTVAL_TYPE_NAMES) {
			pipeline.add(NestedAnnotationRemover.createDescription(annoType));
		}

		AnalysisEngineDescription uriReplacerDesc = createPrimitiveDescription(
				FeatureValueReplacer.class,
				PARAM_ANNO_TYPE, DocumentMetadata.class.getName(),
				PARAM_FEATURE_PATH, "sourceUri",
				PARAM_PATTERN, "file:.+/([^/]+)$",
				PARAM_REPLACE_BY, "file:$1");
		AnalysisEngineDescription xmiWriterDesc = XmiWriter.createDescription(outputDir, false);
		pipeline.add(uriReplacerDesc);
		pipeline.add(xmiWriterDesc);

		SimplePipeline.runPipeline(colReaderDesc,
				pipeline.toArray(new AnalysisEngineDescription[0]));
	}
}