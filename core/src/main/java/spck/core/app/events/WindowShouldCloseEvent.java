package spck.core.app.events;

import spck.core.eventbus.Event;

public class WindowShouldCloseEvent implements Event {
    public static final String key = "spckWindowShouldClose";

    @Override
    public String getKey() {
        return key;
    }
}
