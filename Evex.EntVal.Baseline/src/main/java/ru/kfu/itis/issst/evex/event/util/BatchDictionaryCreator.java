/**
 * 
 */
package ru.kfu.itis.issst.evex.event.util;

import static org.apache.commons.io.filefilter.FileFilterUtils.fileFileFilter;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.annotator.dict_annot.dictionary.impl.DictionaryCreator;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class BatchDictionaryCreator {

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Usage: <dir-with-txt-dict-files>");
			System.exit(1);
		}
		File dictDir = new File(args[0]);
		if (!dictDir.isDirectory()) {
			System.err.println(dictDir + " is not an existing directory");
			System.exit(1);
		}
		for (File txt : dictDir.listFiles((FileFilter) fileFileFilter())) {
			String basename = FilenameUtils.getBaseName(txt.getPath());
			File xml = new File(dictDir, "dict-evtrig-" + basename + ".xml");
			DictionaryCreator.createDictionary(
					txt.getPath(), "UTF-8", xml.getPath(),
					null, null, null, null);
		}
	}
}
