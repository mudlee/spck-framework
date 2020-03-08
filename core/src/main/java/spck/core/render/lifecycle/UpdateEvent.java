package spck.core.render.lifecycle;

import spck.core.eventbus.Event;

public class UpdateEvent implements Event {
	public static final String key = "spckUpdate";
	public float time;
	public float frameTime;

	public void set(float time, float frameTime) {
		this.time = time;
		this.frameTime = frameTime;
	}

	@Override
	public String getKey() {
		return key;
	}
}
