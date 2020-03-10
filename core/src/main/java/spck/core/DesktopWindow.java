package spck.core;

import org.joml.Vector2d;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.eventbus.MessageBus;
import spck.core.render.lifecycle.WindowResizedEvent;

import java.nio.DoubleBuffer;
import java.util.function.Consumer;

import static org.lwjgl.bgfx.BGFX.BGFX_RESET_VSYNC;
import static org.lwjgl.bgfx.BGFX.bgfx_reset;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class DesktopWindow {
	private static final Logger log = LoggerFactory.getLogger(DesktopWindow.class);
	private final DoubleBuffer mouseCursorAbsolutePositionX = MemoryUtil.memAllocDouble(1);
	private final DoubleBuffer mouseCursorAbsolutePositionY = MemoryUtil.memAllocDouble(1);
	private final DesktopWindowPreferences preferences;
	private final MessageBus appLifeCycle;
	private final WindowResizedEvent windowResizedEvent = new WindowResizedEvent();
	private long id;
	private int resolutonFormat;

	public DesktopWindow(DesktopWindowPreferences preferences, MessageBus appLifeCycle) {
		this.preferences = preferences;
		this.appLifeCycle = appLifeCycle;
	}

	public void create() {
		log.debug("Initialising window with preferences {}", preferences);

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

		id = glfwCreateWindow(preferences.width, preferences.height, preferences.title, NULL, NULL);

		if (id == 0) {
			throw new RuntimeException("Error creating GLFW window");
		}

		glfwSetFramebufferSizeCallback(id, this::resize);
	}

	public Consumer<Vector2d> getCursorPositionConsumer() {
		return target -> {
			mouseCursorAbsolutePositionX.clear();
			mouseCursorAbsolutePositionY.clear();
			glfwGetCursorPos(id, mouseCursorAbsolutePositionX, mouseCursorAbsolutePositionY);
			target.set(mouseCursorAbsolutePositionX.get(), mouseCursorAbsolutePositionY.get());
		};
	}

	public void setResolutionFormat(int format) {
		this.resolutonFormat = format;
	}

	public long getId() {
		return id;
	}

	public int getWidth() {
		return preferences.width;
	}

	public int getHeight() {
		return preferences.height;
	}

	private void resize(long window, int width, int height) {
		preferences.width = width;
		preferences.height = height;
		bgfx_reset(width, height, BGFX_RESET_VSYNC, resolutonFormat);

		windowResizedEvent.set(width,height);
		appLifeCycle.broadcast(windowResizedEvent);
	}
}
