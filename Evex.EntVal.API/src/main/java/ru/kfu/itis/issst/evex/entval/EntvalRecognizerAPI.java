/**
 * 
 */
package ru.kfu.itis.issst.evex.entval;

import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.TypeSystemDescriptionFactory;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class EntvalRecognizerAPI {

	public static final String TYPESYSTEM_ENTVAL = "ru.kfu.itis.issst.evex.entval.entval-ts";

	public static TypeSystemDescription getTypeSystemDescription() {
		return TypeSystemDescriptionFactory.createTypeSystemDescription(TYPESYSTEM_ENTVAL);
	}

	private EntvalRecognizerAPI() {
	}
}
