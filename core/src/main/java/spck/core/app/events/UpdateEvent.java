package spck.core.app.events;

import spck.core.eventbus.Event;

public class UpdateEvent implements Event {
	public static final String key = "spckUpdate";

	/**
	 * The delta between
	 */
	public float deltaTime;

	/**
	 * Length of the last frame in seconds
	 */
	public float frameTime;

	public void set(float time, float frameTime) {
		this.deltaTime = time;
		this.frameTime = frameTime;
	}

	@Override
	public String getKey() {
		return key;
	}
}
