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
	public final DataType dataType;
	private static final Logger log = LoggerFactory.getLogger(Renderer.class);
	private final GraphicsContext context;
	private final ConcurrentQueue<SubmitCommand> commandQueue = new PushPullConcurrentQueue<>(1000);

	public Renderer(RendererBackend backend, boolean debug) {
		switch (backend) {
			case OPENGL:
				this.context = new OpenGLContext(debug);
				this.dataType = new OpenGLDataType();
				GraphicsData.backend = RendererBackend.OPENGL;
				break;
			default:
				throw new UnsupportedOperationException();
		}
	}

	public void init(long windowId) {
		context.init(windowId);
	}

	public void windowCreated(long windowId, int width, int height, boolean vsync) {
		context.windowCreated(windowId, width, height, vsync);
	}

	public void windowResized(int width, int height) {
		context.windowResized(width, height);
	}

	public void setClearColor(Vector4f color) {
		context.setClearColor(color);
	}

	public void setClearFlags(int mask) {
		context.setClearFlags(mask);
	}

	public void clear() {
		context.clear();
	}

	public void startScene(Camera camera) {
		// TBD: lights, camera, etc
	}

	public void submit(SubmitCommand command) {
		if (!commandQueue.offer(command)) {
			log.warn("Could not submit '{}', no more slot available", command.describe());
		}
	}

	public void endScene() {
		// TBD, maybe batching, sorting
	}

	public void swapBuffers(float frameTime) {
		context.swapBuffers(frameTime, commandQueue);
	}

	public void dispose() {
		context.dispose();
	}
}
