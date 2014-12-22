/**
 * 
 */
package ru.kfu.itis.issst.evex.event.util;

import static org.uimafit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import java.util.List;
import java.util.Set;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;

import ru.kfu.itis.issst.evex.Event;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class Events {

	static public Set<String> getEventTypes() {
		return eventTypes;
	}

	static private final Set<String> eventTypes;

	static {
		try {
			TypeSystemDescription tsd = createTypeSystemDescription(
					"ru.kfu.itis.issst.evex.entval.entval-ts");
			TypeSystem ts = CasCreationUtils.createCas(tsd, null, null).getTypeSystem();
			Type eventSuperType = ts.getType(Event.class.getName());
			List<Type> eventTypesList = ts.getDirectSubtypes(eventSuperType);
			eventTypes = ImmutableSet.copyOf(
					Lists.transform(eventTypesList, new Function<Type, String>() {
						@Override
						public String apply(Type input) {
							return input.getName();
						}
					}));
		} catch (Exception e) {
			throw new IllegalStateException();
		}
	}

	private Events() {
	}
}
