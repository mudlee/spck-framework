package spck.core.renderer;

import com.conversantmedia.util.concurrent.ConcurrentQueue;
import org.joml.Vector4f;

public abstract class GraphicsContext {
    public abstract void init();

    public abstract void windowCreated(long windowId, int windowWidth, int windowHeight, boolean vSync, boolean debug);

    public abstract void setClearFlags(int mask);

    public abstract void setClearColor(Vector4f color);

    public abstract void clear();

    public abstract void swapBuffers(float frameTime, ConcurrentQueue<SubmitCommand> commandQueue);

    public abstract void windowResized(int newWidth, int newHeight);

    public abstract void dispose();

}
