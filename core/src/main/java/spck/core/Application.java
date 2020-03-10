package spck.core;

import org.lwjgl.bgfx.BGFXInit;
import org.lwjgl.bgfx.BGFXPlatformData;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWNativeX11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.eventbus.MessageBus;
import spck.core.input.Input;
import spck.core.render.MainRenderer;
import spck.core.render.bgfx.VertexLayoutContext;
import spck.core.render.lifecycle.*;

import static org.lwjgl.bgfx.BGFX.*;
import static org.lwjgl.bgfx.BGFXPlatform.bgfx_set_platform_data;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.APIUtil.DEBUG_STREAM;
import static org.lwjgl.system.MemoryUtil.NULL;

abstract public class Application {
	private static final Logger log = LoggerFactory.getLogger(Application.class);
	private final FrameStartEvent frameStartEvent = new FrameStartEvent();
	private final UpdateEvent updateEvent = new UpdateEvent();
	private final AfterUpdateEvent afterUpdateEvent = new AfterUpdateEvent();
	private final FrameEndEvent frameEndEvent = new FrameEndEvent();
	protected final DesktopWindow window;
	protected final Input input;
	protected final MessageBus appLifeCycle = new MessageBus();
	protected MainRenderer mainRenderer;

	public Application(DesktopWindowPreferences windowPreferences) {
		this.window = new DesktopWindow(windowPreferences, appLifeCycle);
		this.input = new Input(appLifeCycle);
	}

	public void run() {
		log.info("Starting up application");

		if (!glfwInit()) {
			throw new RuntimeException("Error initializing GLFW");
		}

		log.debug("Creating Window");
		window.create();
		log.debug("Creating input");
		input.create(window.getWidth(), window.getHeight(), window.getCursorPositionConsumer());

		// INPUT HANDLING
		log.debug("Setting up input callbacks");
		glfwSetKeyCallback(window.getId(), (window, key, scancode, action, mods) -> input.keyCallback(key, scancode, action, mods));
		glfwSetCursorPosCallback(window.getId(), (window, x, y) -> input.cursorPosCallback(x, y));
		glfwSetScrollCallback(window.getId(), (window, xOffset, yOffset) -> input.mouseScrollCallback(xOffset, yOffset));
		glfwSetMouseButtonCallback(window.getId(), (window, button, action, mods) -> input.mouseButtonCallback(button, action, mods));

		initBgfx();

		appLifeCycle.broadcast(new InitializedEvent());
		log.debug("Application has been initialized");

		loop();

		log.info("Application is shutting down");
		appLifeCycle.broadcast(new DisposeEvent());
		bgfx_shutdown();

		glfwFreeCallbacks(window.getId());
		glfwDestroyWindow(window.getId());
		glfwTerminate();
		log.info("Terminated");
	}

	private void loop() {
		long lastTime;
		long startTime = lastTime = glfwGetTimerValue();
		while (!glfwWindowShouldClose(window.getId())) {
			appLifeCycle.broadcast(frameStartEvent);
			glfwPollEvents();

			long now = glfwGetTimerValue();
			long frameTime = now - lastTime;
			lastTime = now;

			double freq = glfwGetTimerFrequency();
			double toMs = 1000.0 / freq;
			double time = (now - startTime) / freq;

			bgfx_set_view_rect(0, 0, 0, window.getWidth(), window.getHeight());
			bgfx_dbg_text_clear(0, false);

			updateEvent.set((float) time, (float) (frameTime * toMs));
			appLifeCycle.broadcast(updateEvent);
			appLifeCycle.broadcast(afterUpdateEvent);

			bgfx_frame(false);
			appLifeCycle.broadcast(frameEndEvent);
		}
	}

	private void initBgfx() {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			bgfx_set_platform_data(setupPlatform(stack));
			reportRenderers();

			BGFXInit init = BGFXInit.mallocStack(stack);
			bgfx_init_ctor(init);

			init
					.type(BGFX_RENDERER_TYPE_COUNT)
					.vendorId(BGFX_PCI_ID_NONE)
					.deviceId((short) 0)
					.callback(null)
					.allocator(null)
					.resolution(it -> it
							.width(window.getWidth())
							.height(window.getHeight())
							.reset(BGFX_RESET_VSYNC));
			if (!bgfx_init(init)) {
				throw new RuntimeException("Error initializing bgfx renderer");
			}

			window.setResolutionFormat(init.resolution().format());

			int renderer = bgfx_get_renderer_type();
			VertexLayoutContext.setup(renderer);

			String rendererName = bgfx_get_renderer_name(renderer);
			if ("NULL".equals(rendererName)) {
				throw new RuntimeException("Error identifying bgfx renderer");
			}

			DEBUG_STREAM.println("Used renderer: "+rendererName);

			bgfx_set_debug(BGFX_DEBUG_TEXT);
			bgfx_set_view_clear(0,
					BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH,
					0x303030ff,
					1.0f,
					0);

			mainRenderer = new MainRenderer(renderer);
		}
	}

	private void reportRenderers() {
		int[] rendererTypes = new int[BGFX_RENDERER_TYPE_COUNT];
		int count = bgfx_get_supported_renderers(rendererTypes);

		DEBUG_STREAM.println("Supported renderers:");

		for (int i = 0; i < count; i++) {
			DEBUG_STREAM.println("" + bgfx_get_renderer_name(rendererTypes[i]));
		}
	}

	private BGFXPlatformData setupPlatform(MemoryStack stack) {
		BGFXPlatformData platformData = BGFXPlatformData.callocStack(stack);

		switch (Platform.get()) {
			case LINUX:
				platformData.ndt(GLFWNativeX11.glfwGetX11Display());
				platformData.nwh(GLFWNativeX11.glfwGetX11Window(window.getId()));
				break;
			case MACOSX:
				platformData.ndt(NULL);
				platformData.nwh(GLFWNativeCocoa.glfwGetCocoaWindow(window.getId()));
				break;
			case WINDOWS:
				platformData.ndt(NULL);
				platformData.nwh(GLFWNativeWin32.glfwGetWin32Window(window.getId()));
				break;
		}

		platformData.context(NULL);
		platformData.backBuffer(NULL);
		platformData.backBufferDS(NULL);

		return platformData;
	}
}
