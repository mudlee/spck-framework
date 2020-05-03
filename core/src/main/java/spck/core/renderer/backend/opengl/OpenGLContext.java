package spck.core.renderer.backend.opengl;

import com.conversantmedia.util.concurrent.ConcurrentQueue;
import org.joml.Vector4f;
import org.lwjgl.opengl.GLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.GraphicsContext;
import spck.core.renderer.SubmitCommand;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL41.*;

public class OpenGLContext extends GraphicsContext {
	private static final Logger log = LoggerFactory.getLogger(OpenGLContext.class);
	private int clearFlags = 0;
	private long windowId;

	@Override
	public void init() {
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
	}

	@Override
	public void windowCreated(long windowId, int windowWidth, int windowHeight, boolean vSync, boolean debug) {
		log.debug("Initializing OpenGL context...");
		this.windowId = windowId;

		glfwMakeContextCurrent(this.windowId);

		createCapabilities();

		if (debug) {
			GLUtil.setupDebugMessageCallback();
		}

		log.debug("Initialized");
		log.debug("\tOpenGL Vendor: {}", glGetString(GL_VENDOR));
		log.debug("\tVersion: {}", glGetString(GL_VERSION));
		log.debug("\tRenderer: {}", glGetString(GL_RENDERER));
		log.debug("\tShading Language Version: {}", glGetString(GL_SHADING_LANGUAGE_VERSION));

		log.debug("Setting up vsync");
		glfwSwapInterval(vSync ? GLFW_TRUE : GLFW_FALSE);
	}

	@Override
	public void setClearFlags(int mask) {
		this.clearFlags = mask;
	}

	@Override
	public void setClearColor(Vector4f color) {
		glClearColor(color.x, color.y, color.z, color.w);
	}

	@Override
	public void clear() {
		glClear(clearFlags);
	}

	@Override
	public void swapBuffers(float frameTime, ConcurrentQueue<SubmitCommand> commandQueue) {
		while (!commandQueue.isEmpty()) {
			SubmitCommand command = commandQueue.poll();
			command.getShader().bind();
			command.getVertexArray().bind();

			command.getVertexArray().getIndexBuffer().ifPresentOrElse(
					(buffer) -> glDrawElements(GL_TRIANGLES, buffer.getLength(), GL_UNSIGNED_INT, 0),
					() -> log.error("Cannot render '{}' without a bound index buffer", command.describe())
			);

			command.getVertexArray().unbind();
			command.getShader().unbind();
		}

		glfwSwapBuffers(windowId);
	}

	@Override
	public void windowResized(int newWidth, int newHeight) {
		glViewport(0, 0, newWidth, newHeight);
	}

	@Override
	public void dispose() {

	}
}
