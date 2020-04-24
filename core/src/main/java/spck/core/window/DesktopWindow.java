package spck.core.window;

import org.joml.Vector2d;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.eventbus.MessageBus;
import spck.core.app.events.FrameStartEvent;
import spck.core.app.events.WindowResizedEvent;
import spck.core.renderer.GraphicsContext;
import spck.core.renderer.Renderer;
import spck.core.window.input.Input;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class DesktopWindow {
	public final Input input;

	private static final Logger log = LoggerFactory.getLogger(DesktopWindow.class);
	private final DoubleBuffer mouseCursorAbsolutePositionX = MemoryUtil.memAllocDouble(1);
	private final DoubleBuffer mouseCursorAbsolutePositionY = MemoryUtil.memAllocDouble(1);
	private final DesktopWindowPreferences preferences;
	private final WindowResizedEvent windowResizedEvent = new WindowResizedEvent();
	private long id;

	public DesktopWindow(DesktopWindowPreferences preferences) {
		this.preferences = preferences;
		this.input = new Input();
		MessageBus.global.subscribe(FrameStartEvent.key, org.lwjgl.glfw.GLFW::glfwPollEvents);
	}

	public void initialize() {
		log.debug("Initialising window with preferences {}", preferences);

		if (!glfwInit()) {
			throw new RuntimeException("Error initializing GLFW");
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

		id = glfwCreateWindow(preferences.width, preferences.height, preferences.title, NULL, NULL);

		if (id == 0) {
			throw new RuntimeException("Error creating GLFW window");
		}

		glfwSetFramebufferSizeCallback(id, this::resize);

		log.debug("Creating input");
		input.initialize(new Input.InitializationParams(preferences.width, preferences.height, this::cursorPositionHasChanged));

		log.debug("Setting up input callbacks");
		glfwSetKeyCallback(id, (window, key, scancode, action, mods) -> input.keyCallback(key, scancode, action, mods));
		glfwSetCursorPosCallback(id, (window, x, y) -> input.cursorPosCallback(x, y));
		glfwSetScrollCallback(id, (window, xOffset, yOffset) -> input.mouseScrollCallback(xOffset, yOffset));
		glfwSetMouseButtonCallback(id, (window, button, action, mods) -> input.mouseButtonCallback(button, action, mods));

		Renderer.init(id, preferences.width, preferences.height);
	}

	public void dispose() {
		Renderer.dispose();
		glfwFreeCallbacks(id);
		glfwDestroyWindow(id);
		glfwTerminate();
	}

	public void shouldClose() {
		glfwSetWindowShouldClose(id, true);
	}

	public int getWidth() {
		return preferences.width;
	}

	public int getHeight() {
		return preferences.height;
	}

	public Input getInput() {
		return input;
	}

	private void cursorPositionHasChanged(Vector2d target){
		mouseCursorAbsolutePositionX.clear();
		mouseCursorAbsolutePositionY.clear();
		glfwGetCursorPos(id, mouseCursorAbsolutePositionX, mouseCursorAbsolutePositionY);
		target.set(mouseCursorAbsolutePositionX.get(), mouseCursorAbsolutePositionY.get());
	}

	private void resize(long window, int width, int height) {
		Renderer.windowResized(width ,height);

		preferences.width = width;
		preferences.height = height;

		windowResizedEvent.set(width,height);
		MessageBus.global.broadcast(windowResizedEvent);
	}
}
