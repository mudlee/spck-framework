package spck.core.render.lifecycle;

import spck.core.eventbus.Event;

public class FrameEndEvent implements Event {
	public static final String key = "spckFrameEnd";

	@Override
	public String getKey() {
		return key;
	}
}
