package spck.core.renderer;

import com.conversantmedia.util.concurrent.ConcurrentQueue;
import com.conversantmedia.util.concurrent.PushPullConcurrentQueue;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.backend.bgfx.BGFXContext;
import spck.core.renderer.backend.bgfx.BGFXDataType;

public class Renderer {
    public static final RendererApi API = RendererApi.BGFX;
    public static final DataType dataType = new BGFXDataType();
    private static final Logger log = LoggerFactory.getLogger(Renderer.class);
    private static final GraphicsContext context = new BGFXContext();
    private static final ConcurrentQueue<SubmitCommand> commandQueue = new PushPullConcurrentQueue<>(10); // TODO

    public static void init(long windowId, int width, int height) {
        context.init(windowId,width,height);
    }

    public static void windowResized(int width, int height) {
        context.windowResized(width,height);
    }

    public static void setClearColor(Vector4f color) {
        context.setClearColor(color);
    }

    public static void clear() {
        context.clear();
    }

    public static void startScene() {
        // TBD: lights, camera, etc
    }

    public static void submit(SubmitCommand command) {
        if(!commandQueue.offer(command)){
            log.warn("Could not submit '{}'", command.describe());
        }
    }

    public static void endScene() {
        // TBD, maybe batching, sorting
    }

    public static void swapBuffers() {
        context.swapBuffers(commandQueue);
    }

    public static void dispose() {
        context.dispose();
    }
}
