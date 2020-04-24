package spck.core.app.events;

import spck.core.eventbus.Event;

public class InitializedEvent implements Event {
	public static final String key = "spckInitialize";

	@Override
	public String getKey() {
		return key;
	}
}
