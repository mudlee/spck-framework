package spck.core.render.lifecycle;

import spck.core.eventbus.Event;

public class AfterUpdateEvent implements Event {
	public static final String key = "spckAfterUpdate";

	@Override
	public String getKey() {
		return key;
	}
}
