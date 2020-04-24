package spck.core.eventbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MessageBus {
    public static final MessageBus global = new MessageBus();

    private final List<Consumer<Event>> CONSUMER_EMPTY_LIST = new ArrayList<>();
    private final List<Runnable> RUNNABLE_EMPTY_LIST = new ArrayList<>();
    private final Map<String, List<Consumer<Event>>> consumers = new HashMap<>();
    private final Map<String, List<Runnable>> runnables = new HashMap<>();

    public void broadcast(Event event) {
        for (Consumer<Event> consumer : consumers.getOrDefault(event.getKey(), CONSUMER_EMPTY_LIST)) {
            consumer.accept(event);
        }

        for (Runnable runnable : runnables.getOrDefault(event.getKey(), RUNNABLE_EMPTY_LIST)) {
            runnable.run();
        }
    }

    public void subscribe(String key, Consumer<Event> consumer) {
        consumers.putIfAbsent(key, new ArrayList<>());
        consumers.get(key).add(consumer);
    }

    public void subscribe(String key, Runnable runnable) {
        runnables.putIfAbsent(key, new ArrayList<>());
        runnables.get(key).add(runnable);
    }
}
