package spck.core.app.events;

import spck.core.eventbus.Event;

public class WindowResizedEvent implements Event {
    public static final String key = "spckWindowResized";
    public int width;
    public int height;

    public void set(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public String getKey() {
        return key;
    }
}
