package spck.core.window;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.app.events.FrameStartEvent;
import spck.core.app.events.WindowResizedEvent;
import spck.core.eventbus.MessageBus;
import spck.core.graphics.Antialiasing;
import spck.core.renderer.Renderer;
import spck.core.window.input.Input;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class DesktopWindow {
	public final Input input;

	private static final Logger log = LoggerFactory.getLogger(DesktopWindow.class);
	private final DoubleBuffer mouseCursorAbsolutePositionX = MemoryUtil.memAllocDouble(1);
	private final DoubleBuffer mouseCursorAbsolutePositionY = MemoryUtil.memAllocDouble(1);
	private final DesktopWindowPreferences preferences;
	private final boolean debug;
	private final WindowResizedEvent windowResizedEvent = new WindowResizedEvent();
	private final Vector2f windowScale = new Vector2f();
	private final Vector2i windowSize = new Vector2i();
	private final Vector2i framebufferSize = new Vector2i();
	private GLFWVidMode videoMode;
	private int screenPixelRatio;
	private long id;

	public DesktopWindow(DesktopWindowPreferences preferences, boolean debug) {
		this.preferences = preferences;
		this.debug = debug;
		this.input = new Input();
		this.windowSize.set(preferences.width, preferences.height);
		MessageBus.global.subscribe(FrameStartEvent.key, org.lwjgl.glfw.GLFW::glfwPollEvents);
	}

	public void initialize() {
		log.debug("Initialising window with preferences {}", preferences);

		if (!glfwInit()) {
			throw new RuntimeException("Error initializing GLFW");
		}

		glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

		videoMode = pickMonitor();

		if (preferences.antialiasing != Antialiasing.OFF) {
			glfwWindowHint(GLFW_SAMPLES, preferences.antialiasing.getValue());
		}

		if (preferences.fullscreen) {
			windowSize.set(videoMode.width(), videoMode.height());
			glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
		}

		Renderer.init();
		id = glfwCreateWindow(windowSize.x, windowSize.y, preferences.title, preferences.fullscreen ? glfwGetPrimaryMonitor() : NULL, NULL);

		if (id == NULL) {
			throw new RuntimeException("Error creating GLFW window");
		}
		Renderer.windowCreated(id, windowSize.x, windowSize.y, preferences.vsync, debug);

		glfwSetWindowSizeCallback(id, this::windowResized);
		glfwSetFramebufferSizeCallback(id, this::framebufferResized);
		glfwSetWindowContentScaleCallback(id, this::contentScaleChanged);
		queryFramebufferSize();
		queryContentScale();
		calculateScreenPixelRatio();

		log.debug("Creating input");
		input.initialize(new Input.InitializationParams(windowSize.x, windowSize.y, this::cursorPositionHasChanged));

		log.debug("Setting up input callbacks");
		glfwSetKeyCallback(id, (window, key, scancode, action, mods) -> input.keyCallback(key, scancode, action, mods));
		glfwSetCursorPosCallback(id, (window, x, y) -> input.cursorPosCallback(x, y));
		glfwSetScrollCallback(id, (window, xOffset, yOffset) -> input.mouseScrollCallback(xOffset, yOffset));
		glfwSetMouseButtonCallback(id, (window, button, action, mods) -> input.mouseButtonCallback(button, action, mods));

		if (!preferences.fullscreen) {
			glfwSetWindowPos(id, (videoMode.width() - windowSize.x) / 2, (videoMode.height() - windowSize.y) / 2);
		}

		log.debug("Window has been setup");
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

	public Vector2f getWindowScale() {
		return windowScale;
	}

	public Vector2i getWindowSize() {
		return windowSize;
	}

	public Vector2i getFramebufferSize() {
		return framebufferSize;
	}

	public int getScreenPixelRatio() {
		return screenPixelRatio;
	}

	public Input getInput() {
		return input;
	}

	private GLFWVidMode pickMonitor() {
		PointerBuffer buffer = glfwGetMonitors();
		if (buffer == null) {
			throw new RuntimeException("No monitors were found");
		}

		if (buffer.capacity() == 1) {
			log.info("Found one monitor: {}", glfwGetMonitorName(buffer.get()));
			return glfwGetVideoMode(glfwGetPrimaryMonitor());
		} else {
			log.info("Found multiple monitors:");
			for (int i = 0; i < buffer.capacity(); i++) {
				log.info(" Monitor-{} '{}'", i, glfwGetMonitorName(buffer.get(i)));
			}

			return glfwGetVideoMode(glfwGetPrimaryMonitor());
		}
	}

	private void cursorPositionHasChanged(Vector2d target) {
		mouseCursorAbsolutePositionX.clear();
		mouseCursorAbsolutePositionY.clear();
		glfwGetCursorPos(id, mouseCursorAbsolutePositionX, mouseCursorAbsolutePositionY);
		target.set(mouseCursorAbsolutePositionX.get(), mouseCursorAbsolutePositionY.get());
	}

	private void windowResized(long window, int width, int height) {
		windowSize.set(width, height);
		glViewport(0, 0, width, height);
		calculateScreenPixelRatio();

		Renderer.windowResized(width, height);
		input.windowResized(width, height);

		windowResizedEvent.set(width, height);
		MessageBus.global.broadcast(windowResizedEvent);
		log.debug("Window resized to {}x{}", width, height);
	}

	private void framebufferResized(long window, int width, int height) {
		framebufferSize.set(width, height);
		log.debug("Framebuffer size change to {}x{}", width, height);
	}

	private void queryFramebufferSize() {
		try (MemoryStack stack = stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);

			glfwGetFramebufferSize(id, w, h);
			framebufferSize.set(w.get(0), h.get(0));
		}
	}

	private void contentScaleChanged(long window, float xScale, float yScale) {
		windowScale.set(xScale, yScale);
		log.debug("Window scale changed to x:{} y:{}", windowScale.x, windowScale.y);
	}

	private void queryContentScale() {
		try (MemoryStack stack = stackPush()) {
			FloatBuffer sx = stack.mallocFloat(1);
			FloatBuffer sy = stack.mallocFloat(1);
			glfwGetWindowContentScale(id, sx, sy);
			windowScale.set(sx.get(), sy.get());
			log.debug("Window scale changed to x:{} y:{}", windowScale.x, windowScale.y);
		}
	}

	private void calculateScreenPixelRatio() {
		// https://en.wikipedia.org/wiki/4K_resolution
		int uhdMinWidth = 3840;
		int uhdMinHeight = 1716;
		boolean UHD = videoMode.width() >= uhdMinWidth && videoMode.height() >= uhdMinHeight;
		log.debug("Screen is {}x{}, UHD: {}", videoMode.width(), videoMode.height(), UHD);

		// Check if the monitor is 4K
		if (UHD) {
			screenPixelRatio = 2;
			log.debug("Screen pixel ratio has been set to: {}", screenPixelRatio);
			return;
		}

		IntBuffer widthScreenCoordBuf = MemoryUtil.memAllocInt(1);
		IntBuffer heightScreenCoordBuf = MemoryUtil.memAllocInt(1);
		IntBuffer widthPixelsBuf = MemoryUtil.memAllocInt(1);
		IntBuffer heightPixelsBuf = MemoryUtil.memAllocInt(1);

		glfwGetWindowSize(id, widthScreenCoordBuf, heightScreenCoordBuf);
		glfwGetFramebufferSize(id, widthPixelsBuf, heightPixelsBuf);

		screenPixelRatio = (int) Math.floor((float) widthPixelsBuf.get() / (float) widthScreenCoordBuf.get());
		log.debug("Screen pixel ratio has been set to: {}", screenPixelRatio);

		MemoryUtil.memFree(widthScreenCoordBuf);
		MemoryUtil.memFree(heightScreenCoordBuf);
		MemoryUtil.memFree(widthPixelsBuf);
		MemoryUtil.memFree(heightPixelsBuf);
	}
}
