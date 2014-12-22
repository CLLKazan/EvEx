/**
 * 
 */
package ru.kfu.itis.issst.evex.event.util;

import static ru.kfu.itis.cll.uima.cas.FSTypeUtils.getAnnotationType;
import static ru.kfu.itis.cll.uima.cas.FSTypeUtils.getFeature;
import static ru.kfu.itis.cll.uima.util.DocumentUtils.getDocumentUri;
import static ru.kfu.itis.issst.uima.postagger.MorphCasUtils.getGrammemes;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.opencorpora.cas.Word;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.OperationalProperties;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ru.kfu.cll.uima.segmentation.fstype.Sentence;
import ru.kfu.itis.cll.uima.io.IoUtils;
import ru.kfu.itis.cll.uima.util.DocumentUtils;
import ru.kfu.itis.issst.evex.Event;
import ru.kfu.itis.issst.uima.morph.model.MorphConstants;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
@OperationalProperties(multipleDeploymentAllowed = false)
public class InteractiveTriggerFixer extends JCasAnnotator_ImplBase {

	public static AnalysisEngineDescription createDescription(File changeLogFile)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(
				InteractiveTriggerFixer.class,
				PARAM_CHANGE_LOG_FILE, changeLogFile.getPath());
	}

	public static final String PARAM_CHANGE_LOG_FILE = "changeLogFile";

	@ConfigurationParameter(mandatory = true, name = PARAM_CHANGE_LOG_FILE)
	private File changeLogFile;

	// STATE fields
	private PrintWriter changeLogOut;
	private Scanner sysInScanner;
	// per CAS
	private ChangeLog changeLog;
	private String docUri;

	@Override
	public void initialize(UimaContext ctx) throws ResourceInitializationException {
		super.initialize(ctx);
		try {
			changeLogOut = IoUtils.openPrintWriter(changeLogFile, true);
			changeLogOut.println("# " + new Date());
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		//
		sysInScanner = new Scanner(System.in);
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		IOUtils.closeQuietly(changeLogOut);
		changeLogOut = null;
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		beforeProcess(jCas);
		try {
			for (Sentence sent : JCasUtil.select(jCas, Sentence.class)) {
				for (Event event : JCasUtil.selectCovered(jCas, Event.class, sent)) {
					List<Word> triggerWords = JCasUtil.selectCovered(jCas, Word.class, event);
					if (triggerWords.isEmpty()) {
						changeLog.deleteAnnotation(event, "No words inside");
					} else if (triggerWords.size() > 1) {
						int triggerWordIdx = askUserToSelectSingleTriggerWord(sent, triggerWords);
						if (triggerWordIdx < 0) {
							changeLog.deleteAnnotation(event, "User's choice");
							continue;
						} else {
							Word newTriggerWord = triggerWords.get(triggerWordIdx);
							changeLog.changeSpan(event,
									newTriggerWord.getBegin(),
									newTriggerWord.getEnd(),
									"User's choice");
						}
					} else {
						// a single word
						Word triggerWord = triggerWords.get(0);
						// rejects
						if (!Sets.intersection(
								getGrammemes(triggerWord),
								PRONOUN_GRAMS).isEmpty()) {
							changeLog.deleteAnnotation(event, "Pronoun event trigger");
							continue;
						}
						// changes
						if (event.getBegin() != triggerWord.getBegin() ||
								event.getEnd() != triggerWord.getEnd()) {
							changeLog.changeSpan(event, triggerWord.getBegin(),
									triggerWord.getEnd(),
									"Trigger span must match boundaries of its word");
						}
					}
				}
			}
			changeLog.commit(changeLogOut);
		} finally {
			afterProcess(jCas);
		}
	}

	private int askUserToSelectSingleTriggerWord(Sentence sent, List<Word> triggerWords) {
		// write prompt
		StringBuilder pb = new StringBuilder();
		{
			// write context before
			Word firstWord = triggerWords.get(0);
			Word lastWord = triggerWords.get(triggerWords.size() - 1);
			if (firstWord.getBegin() > sent.getBegin()) {
				pb.append(sent.getCoveredText()
						.substring(0, firstWord.getBegin() - sent.getBegin()));
			}
			// write trigger words with user options
			for (int i = 0; i < triggerWords.size(); i++) {
				Word curWord = triggerWords.get(i);
				// option number
				pb.append('[').append(i + 1).append(']');
				// word text
				pb.append(curWord.getCoveredText());
				// after word
				pb.append(' ');
			}
			// write context after
			if (sent.getEnd() > lastWord.getEnd()) {
				pb.append(sent.getCoveredText()
						.substring(lastWord.getEnd() - sent.getBegin() + 1));
			}
		}
		System.out.println(pb);
		// get user response
		Integer result = null;
		while (result == null) {
			while (result == null) {
				result = tryToParseAsInt(sysInScanner.nextLine());
			}
			if (result > triggerWords.size()) {
				result = null;
			}
		}
		return result - 1;
	}

	private Integer tryToParseAsInt(String line) {
		try {
			return Integer.parseInt(line.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	static private final Set<String> PRONOUN_GRAMS = ImmutableSet.of(
			MorphConstants.NPRO, MorphConstants.Apro);

	private void beforeProcess(JCas jCas) {
		docUri = DocumentUtils.getDocumentUri(jCas);
		changeLog = new ChangeLog();
	}

	private void afterProcess(JCas jCas) {
		changeLog = null;
		docUri = null;
	}
}

class ChangeLog {
	private List<AnnotationAction> actions = Lists.newLinkedList();

	public void deleteAnnotation(AnnotationFS anno, String msg) {
		actions.add(new DeleteAnnotationAction(anno, msg));
	}

	public void changeSpan(AnnotationFS anno, int newBegin, int newEnd, String msg) {
		actions.add(new ChangeSpanAnnotationAction(anno, newBegin, newEnd, msg));
	}

	public void commit(PrintWriter out) {
		for (AnnotationAction a : actions) {
			out.println(a.toTSV());
			a.execute();
		}
	}

	private abstract class AnnotationAction {
		protected String msg;
		protected AnnotationFS anno;

		AnnotationAction(AnnotationFS anno, String msg) {
			this.msg = msg;
			this.anno = anno;
		}

		String toTSV() {
			List<Object> recordElems = Lists.newLinkedList();
			// ACTION
			recordElems.add(getActionName());
			// DOC URI
			recordElems.add(getDocumentUri(anno.getCAS()));
			// ANNOTATION "ID"
			recordElems.add(anno.getType().getName());
			recordElems.add(anno.getBegin());
			recordElems.add(anno.getEnd());
			// action params
			recordElems.addAll(getParameters());
			// msg
			recordElems.add(msg);
			// anno covered text
			recordElems.add(anno.getCoveredText());
			return tabJoiner.join(recordElems);
		}

		abstract protected void execute();

		abstract protected String getActionName();

		abstract protected List<Object> getParameters();
	}

	private class DeleteAnnotationAction extends AnnotationAction {

		@Override
		protected String getActionName() {
			return "DELETE";
		}

		public DeleteAnnotationAction(AnnotationFS anno, String msg) {
			super(anno, msg);
		}

		@Override
		protected List<Object> getParameters() {
			return ImmutableList.of();
		}

		@Override
		protected void execute() {
			anno.getCAS().removeFsFromIndexes(anno);
		}
	}

	private class ChangeSpanAnnotationAction extends AnnotationAction {

		private int newBegin;
		private int newEnd;

		public ChangeSpanAnnotationAction(AnnotationFS anno,
				int newBegin, int newEnd,
				String msg) {
			super(anno, msg);
			this.newBegin = newBegin;
			this.newEnd = newEnd;
		}

		@Override
		protected void execute() {
			CAS cas = anno.getCAS();
			Type uimaAnnoType = getAnnotationType(cas.getTypeSystem());
			Feature beginFeat = getFeature(uimaAnnoType, "begin", true);
			Feature endFeat = getFeature(uimaAnnoType, "end", true);
			//
			cas.removeFsFromIndexes(anno);
			anno.setIntValue(beginFeat, newBegin);
			anno.setIntValue(endFeat, newEnd);
			cas.addFsToIndexes(anno);
		}

		@Override
		protected String getActionName() {
			return "CHANGE-SPAN";
		}

		@Override
		protected List<Object> getParameters() {
			return ImmutableList.<Object> of(newBegin, newEnd);
		}

	}

	static private final Joiner tabJoiner = Joiner.on('\t');
}
