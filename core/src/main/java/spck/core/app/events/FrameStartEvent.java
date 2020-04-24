package spck.core.app.events;

import spck.core.eventbus.Event;

public class FrameStartEvent implements Event {
	public static final String key = "spckFrameStart";

	@Override
	public String getKey() {
		return key;
	}
}
