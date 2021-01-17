package spck.core.renderer;

import com.conversantmedia.util.concurrent.ConcurrentQueue;
import com.conversantmedia.util.concurrent.PushPullConcurrentQueue;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.backend.opengl.OpenGLContext;
import spck.core.renderer.backend.opengl.OpenGLDataType;
import spck.core.renderer.backend.vulkan.VulkanContext;
import spck.core.renderer.backend.vulkan.VulkanDataType;
import spck.core.renderer.camera.Camera;

public class Renderer {
	public final DataType dataType;
	private static final Logger log = LoggerFactory.getLogger(Renderer.class);
	private final GraphicsContext context;
	private final ConcurrentQueue<SubmitCommand> commandQueue = new PushPullConcurrentQueue<>(1000);

	public Renderer(RendererBackend backend) {
		switch (backend) {
			case OPENGL:
				this.context = new OpenGLContext();
				this.dataType = new OpenGLDataType();
				GraphicsData.backend = RendererBackend.OPENGL;
				break;
			case VULKAN:
				this.context = new VulkanContext();
				this.dataType = new VulkanDataType();
				GraphicsData.backend = RendererBackend.VULKAN;
				break;
			default:
				throw new UnsupportedOperationException();
		}
	}

	public void init() {
		context.init();
	}

	public void windowCreated(long windowId, int width, int height, boolean vsync, boolean debug) {
		context.windowCreated(windowId, width, height, vsync, debug);
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
