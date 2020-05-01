package spck.core.renderer;

import com.conversantmedia.util.concurrent.ConcurrentQueue;
import com.conversantmedia.util.concurrent.PushPullConcurrentQueue;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.backend.opengl.OpenGLContext;
import spck.core.renderer.backend.opengl.OpenGLDataType;
import spck.core.renderer.camera.Camera;

public class Renderer {
    public static final DataType dataType = new OpenGLDataType();

    private static final Logger log = LoggerFactory.getLogger(Renderer.class);
    private static final GraphicsContext context = new OpenGLContext();
    private static final ConcurrentQueue<SubmitCommand> commandQueue = new PushPullConcurrentQueue<>(10); // TODO

    public static void init(long windowId, int width, int height, boolean debug) {
        context.init(windowId,width,height, debug);
    }

    public static void windowResized(int width, int height) {
        context.windowResized(width,height);
    }

    public static void setClearColor(Vector4f color) {
        context.setClearColor(color);
    }

    public static void setClearFlags(int mask){
        context.setClearFlags(mask);
    }

    public static void clear() {
        context.clear();
    }

    public static void startScene(Camera camera) {
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

    public static void swapBuffers(float frameTime) {
        context.swapBuffers(frameTime, commandQueue);
    }

    public static void dispose() {
        context.dispose();
    }
}