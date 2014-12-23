/**
 * 
 */
package ru.kfu.itis.issst.evex.util;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createDescription;
import static org.uimafit.factory.ExternalResourceFactory.createExternalResourceDescription;
import static org.uimafit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.pipeline.SimplePipeline;

import ru.kfu.cll.uima.segmentation.fstype.Sentence;
import ru.kfu.cll.uima.tokenizer.fstype.Token;
import ru.kfu.itis.cll.uima.cpe.XmiFileListReader;
import ru.kfu.itis.cll.uima.util.Slf4jLoggerImpl;
import ru.kfu.itis.issst.evex.DeclareBankruptcy;
import ru.kfu.itis.issst.evex.Die;
import ru.kfu.itis.issst.evex.EndOrg;
import ru.kfu.itis.issst.evex.EndPosition;
import ru.kfu.itis.issst.evex.Facility;
import ru.kfu.itis.issst.evex.Fine;
import ru.kfu.itis.issst.evex.GPE;
import ru.kfu.itis.issst.evex.IndictSue;
import ru.kfu.itis.issst.evex.Injure;
import ru.kfu.itis.issst.evex.Location;
import ru.kfu.itis.issst.evex.MergeOrg;
import ru.kfu.itis.issst.evex.Money;
import ru.kfu.itis.issst.evex.Organization;
import ru.kfu.itis.issst.evex.Person;
import ru.kfu.itis.issst.evex.StartOrg;
import ru.kfu.itis.issst.evex.StartPosition;
import ru.kfu.itis.issst.evex.Time;
import ru.kfu.itis.issst.evex.TransferMoney;
import ru.kfu.itis.issst.evex.TransferOwnership;
import ru.kfu.itis.issst.evex.TrialHearing;
import ru.kfu.itis.issst.evex.entval.EntvalRecognizerAPI;
import ru.kfu.itis.issst.uima.consumer.cao.CAOWriter;
import ru.kfu.itis.issst.uima.consumer.cao.impl.MysqlJdbcCasAccessObject;
import ru.kfu.itis.issst.uima.postagger.PosTaggerAPI;
import ru.kfu.itis.issst.uima.segmentation.SentenceSplitterAPI;
import ru.kfu.itis.issst.uima.tokenizer.TokenizerAPI;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class LoadGoldXMICorpus {

	public static void main(String[] args) throws UIMAException, IOException {
		Slf4jLoggerImpl.forceUsingThisImplementation();
		LoadGoldXMICorpus obj = new LoadGoldXMICorpus();
		new JCommander(obj, args);
		obj.run();
	}

	@Parameter(names = { "-c" }, required = true)
	private File corpusBaseDir;

	@Parameter(names = { "-l" }, required = true)
	private File docListFile;

	@Parameter(names = { "--ds" }, required = true)
	private File dsConfigFile;

	private final TypeSystemDescription inputTSD = createTypeSystemDescription(
			"ru.kfu.itis.cll.uima.commons.Commons-TypeSystem",
			TokenizerAPI.TYPESYSTEM_TOKENIZER,
			SentenceSplitterAPI.TYPESYSTEM_SENTENCES,
			PosTaggerAPI.TYPESYSTEM_POSTAGGER,
			EntvalRecognizerAPI.TYPESYSTEM_ENTVAL);

	private LoadGoldXMICorpus() {
	}

	private void run() throws UIMAException, IOException {
		Class<?>[] typesToPersist = {
				// COMMONS
				Token.class,
				// ENTITIES
				Person.class,
				Organization.class,
				GPE.class,
				Location.class,
				Facility.class,
				// VALUES
				Time.class,
				Money.class,
				// EVENTS
				Injure.class,
				Die.class,
				StartPosition.class,
				EndPosition.class,
				TransferOwnership.class,
				TransferMoney.class,
				StartOrg.class,
				EndOrg.class,
				MergeOrg.class,
				DeclareBankruptcy.class,
				IndictSue.class,
				TrialHearing.class,
				ru.kfu.itis.issst.evex.Sentence.class,
				Fine.class
		};
		List<String> typeNamesToPersist = Lists.transform(ImmutableList.copyOf(typesToPersist),
				new Function<Class<?>, String>() {
					@Override
					public String apply(Class<?> cl) {
						return cl.getName();
					}
				});

		CollectionReaderDescription colReaderDesc = createDescription(XmiFileListReader.class,
				inputTSD,
				XmiFileListReader.PARAM_BASE_DIR, corpusBaseDir.getPath(),
				XmiFileListReader.PARAM_LIST_FILE, docListFile.getPath());

		ExternalResourceDescription caoDesc = createExternalResourceDescription(
				MysqlJdbcCasAccessObject.class,
				dsConfigFile);

		AnalysisEngineDescription caoWriterDesc = createPrimitiveDescription(CAOWriter.class,
				CAOWriter.RESOURCE_DAO, caoDesc,
				CAOWriter.PARAM_TYPES_TO_PERSIST, typeNamesToPersist,
				CAOWriter.PARAM_SPAN_TYPE, Sentence.class.getName(),
				CAOWriter.PARAM_DOC_METADATA_DOCUMENT_SIZE, "documentSize");

		SimplePipeline.runPipeline(colReaderDesc, caoWriterDesc);
	}
}