package spck.core.window;

import org.joml.Vector2d;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.eventbus.MessageBus;
import spck.core.app.events.FrameStartEvent;
import spck.core.app.events.WindowResizedEvent;
import spck.core.graphics.Antialiasing;
import spck.core.renderer.Renderer;
import spck.core.window.input.Input;

import java.nio.DoubleBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class DesktopWindow {
	public final Input input;

	private static final Logger log = LoggerFactory.getLogger(DesktopWindow.class);
	private final DoubleBuffer mouseCursorAbsolutePositionX = MemoryUtil.memAllocDouble(1);
	private final DoubleBuffer mouseCursorAbsolutePositionY = MemoryUtil.memAllocDouble(1);
	private final DesktopWindowPreferences preferences;
	private final boolean debug;
	private final WindowResizedEvent windowResizedEvent = new WindowResizedEvent();
	private GLFWVidMode videoMode;
	private long id;

	public DesktopWindow(DesktopWindowPreferences preferences, boolean debug) {
		this.preferences = preferences;
		this.debug = debug;
		this.input = new Input();
		MessageBus.global.subscribe(FrameStartEvent.key, org.lwjgl.glfw.GLFW::glfwPollEvents);
	}

	public void initialize() {
		log.debug("Initialising window with preferences {}", preferences);

		if (!glfwInit()) {
			throw new RuntimeException("Error initializing GLFW");
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

		videoMode = pickMonitor();

		if (preferences.antialiasing != Antialiasing.OFF) {
			glfwWindowHint(GLFW_SAMPLES, preferences.antialiasing.getValue());
		}

		if (preferences.fullscreen) {
			preferences.width = videoMode.width();
			preferences.height = videoMode.height();
			glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
		}

		id = glfwCreateWindow(preferences.width, preferences.height, preferences.title, preferences.fullscreen ? glfwGetPrimaryMonitor() : NULL, NULL);

		if (id == NULL) {
			throw new RuntimeException("Error creating GLFW window");
		}

		glfwMakeContextCurrent(id);
		glfwSetWindowSizeCallback(id, this::resize);

		log.debug("Creating input");
		input.initialize(new Input.InitializationParams(preferences.width, preferences.height, this::cursorPositionHasChanged));

		log.debug("Setting up input callbacks");
		glfwSetKeyCallback(id, (window, key, scancode, action, mods) -> input.keyCallback(key, scancode, action, mods));
		glfwSetCursorPosCallback(id, (window, x, y) -> input.cursorPosCallback(x, y));
		glfwSetScrollCallback(id, (window, xOffset, yOffset) -> input.mouseScrollCallback(xOffset, yOffset));
		glfwSetMouseButtonCallback(id, (window, button, action, mods) -> input.mouseButtonCallback(button, action, mods));

		log.debug("Setting up vsync");
		glfwSwapInterval(preferences.vsync ? GLFW_TRUE : GLFW_FALSE);

		Renderer.init(id, preferences.width, preferences.height, debug);

		if(!preferences.fullscreen) {
			glfwSetWindowPos(id,(videoMode.width() - preferences.width) / 2,(videoMode.height() - preferences.height) / 2);
		}

		glfwShowWindow(id);
	}

	public void dispose() {
		Renderer.dispose();
		MemoryUtil.memFree(mouseCursorAbsolutePositionX);
		MemoryUtil.memFree(mouseCursorAbsolutePositionY);
		glfwFreeCallbacks(id);
		glfwDestroyWindow(id);
		glfwTerminate();
		Objects.requireNonNull(glfwSetErrorCallback(null)).free();
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

	private GLFWVidMode pickMonitor() {
		PointerBuffer buffer = glfwGetMonitors();
		if(buffer == null) {
			throw new RuntimeException("No monitors were found");
		}

		if(buffer.capacity() == 1){
			log.info("Found one monitor: {}", glfwGetMonitorName(buffer.get()));
			return glfwGetVideoMode(glfwGetPrimaryMonitor());
		}
		else {
			// TODO: write a monitor picker here
			log.info("Found multiple monitors:");
			for (int i = 0; i < buffer.capacity(); i++) {
				log.info(" Monitor-{} '{}'", i, glfwGetMonitorName(buffer.get(i)));
			}

			return glfwGetVideoMode(glfwGetPrimaryMonitor());
		}
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
