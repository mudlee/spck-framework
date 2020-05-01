package spck.core.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.app.events.*;
import spck.core.eventbus.MessageBus;
import spck.core.renderer.Renderer;
import spck.core.renderer.backend.RendererApi;
import spck.core.window.DesktopWindow;
import spck.core.window.DesktopWindowPreferences;

public abstract class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private final FrameStartEvent frameStartEvent = new FrameStartEvent();
    private final UpdateEvent updateEvent = new UpdateEvent();
    private boolean running;
    protected final DesktopWindow window;

    public Application(DesktopWindowPreferences windowPreferences, boolean debug) {
        RendererApi.backend = windowPreferences.rendererBackend;
        this.window = new DesktopWindow(windowPreferences, debug);
    }

    public final void run(){
        log.info("Starting up application...");

        window.initialize();

        MessageBus.global.broadcast(new InitializedEvent());
        log.debug("Application has been initialized");

        loop();

        log.info("Application is shutting down");
        MessageBus.global.broadcast(new DisposeEvent());
        window.dispose();
        log.info("Terminated");
    }

    public final void stop(){
        window.shouldClose();
        running = false;
    }

    private void loop() {
        running = true;

        long lastTime = System.nanoTime();

        while (running) {
            MessageBus.global.broadcast(frameStartEvent);
            long now = System.nanoTime();
            long frameTimeNanos = now - lastTime;
            float frameTime = frameTimeNanos / 1_000_000_000f;
            lastTime = now;

            // TODO
            updateEvent.set(60f, frameTime);
            Renderer.swapBuffers(frameTime);

            MessageBus.global.broadcast(updateEvent);
        }
    }
}
