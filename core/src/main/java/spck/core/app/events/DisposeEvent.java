package spck.core.app.events;

import spck.core.eventbus.Event;

public class DisposeEvent implements Event {
	public static final String key = "spckDispose";

	@Override
	public String getKey() {
		return key;
	}
}
