package spck.core.renderer.backend.bgfx;

import com.conversantmedia.util.concurrent.ConcurrentQueue;
import org.joml.Vector4f;
import org.lwjgl.bgfx.BGFXInit;
import org.lwjgl.bgfx.BGFXPlatformData;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWNativeX11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.GraphicsContext;
import spck.core.renderer.SubmitCommand;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.bgfx.BGFX.*;
import static org.lwjgl.bgfx.BGFX.BGFX_CLEAR_DEPTH;
import static org.lwjgl.bgfx.BGFXPlatform.bgfx_set_platform_data;
import static org.lwjgl.system.MemoryUtil.NULL;

// TODO
// - multhtreaded submission with encoders https://bkaradzic.github.io/bgfx/bgfx.html#encoder
public class BGFXContext extends GraphicsContext {
    public static int renderer; // TODO: it's maybe not good here

    private static final Logger log = LoggerFactory.getLogger(BGFXContext.class);
    private int resolutionFormat;
    private int windowWidth;
    private int windowHeight;

    @Override
    public void init(long windowId, int windowWidth, int windowHeight) {
        log.debug("Initializing BGFX context...");
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            bgfx_set_platform_data(setupPlatform(stack, windowId));
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
                            .width(windowWidth)
                            .height(windowHeight)
                            .reset(BGFX_RESET_VSYNC));
            if (!bgfx_init(init)) {
                throw new RuntimeException("Error initializing bgfx renderer");
            }

            resolutionFormat = init.resolution().format();

            renderer = bgfx_get_renderer_type();

            String rendererName = bgfx_get_renderer_name(renderer);
            if ("NULL".equals(rendererName)) {
                throw new RuntimeException("Error identifying bgfx renderer");
            }

            log.debug("Used renderer: "+rendererName);
        }

        log.debug("BGFX context has been initialized");
    }

    @Override
    public void setClearColor(Vector4f color) {
        // todo convert color to hex rgba
    }

    @Override
    public void clear() {
        bgfx_set_view_clear(0,
                BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH,
                0x303030ff, // TODO use the saved color or default
                1.0f,
                0);
    }

    @Override
    public void swapBuffers(ConcurrentQueue<SubmitCommand> commandQueue) {
        bgfx_set_view_rect(0, 0, 0, windowWidth, windowHeight);

        while (!commandQueue.isEmpty()) {
            SubmitCommand command = commandQueue.poll();
            command.getVertexArray().bind();
            bgfx_submit( 0, command.getShader().getProgram(), 0, false);
        }

        bgfx_frame(false);
    }

    @Override
    public void windowResized(int windowWidth, int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        bgfx_reset(windowWidth, windowHeight, BGFX_RESET_VSYNC, resolutionFormat);
    }

    @Override
    public void dispose() {
        log.debug("Disposing BGFX context");
        bgfx_shutdown();
    }

    private BGFXPlatformData setupPlatform(MemoryStack stack, long windowId) {
        BGFXPlatformData platformData = BGFXPlatformData.callocStack(stack);

        switch (Platform.get()) {
            case LINUX:
                platformData.ndt(GLFWNativeX11.glfwGetX11Display());
                platformData.nwh(GLFWNativeX11.glfwGetX11Window(windowId));
                break;
            case MACOSX:
                platformData.ndt(NULL);
                platformData.nwh(GLFWNativeCocoa.glfwGetCocoaWindow(windowId));
                break;
            case WINDOWS:
                platformData.ndt(NULL);
                platformData.nwh(GLFWNativeWin32.glfwGetWin32Window(windowId));
                break;
            default:
                throw new UnsupportedOperationException("Unsupported platform: "+Platform.get().getName());
        }

        platformData.context(NULL);
        platformData.backBuffer(NULL);
        platformData.backBufferDS(NULL);

        return platformData;
    }

    private void reportRenderers() {
        int[] rendererTypes = new int[BGFX_RENDERER_TYPE_COUNT];

        List<String> renderers = new ArrayList<>();
        for (int i = 0; i < bgfx_get_supported_renderers(rendererTypes); i++) {
            renderers.add(bgfx_get_renderer_name(rendererTypes[i]));
        }

        log.debug("Supported renderers: {}", String.join(", ", renderers));
    }
}
