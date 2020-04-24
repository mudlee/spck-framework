package spck.core.renderer;

import com.conversantmedia.util.concurrent.ConcurrentQueue;
import org.joml.Vector4f;

public abstract class GraphicsContext {
    public abstract void init(long windowId, int windowWidth, int windowHeight);

    public abstract void setClearColor(Vector4f color);

    public abstract void clear();

    public abstract void swapBuffers(ConcurrentQueue<SubmitCommand> commandQueue);

    public abstract void windowResized(int newWidth, int newHeight);

    public abstract void dispose();
}
