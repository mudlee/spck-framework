package spck.core.renderer.backend.opengl;

import com.conversantmedia.util.concurrent.ConcurrentQueue;
import org.joml.Vector4f;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.KHRDebug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.GraphicsContext;
import spck.core.renderer.SubmitCommand;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL41.*;

public class OpenGLContext extends GraphicsContext {
    private static final Logger log = LoggerFactory.getLogger(OpenGLContext.class);
    private int clearFlags = 0;
    private long windowId;

    @Override
    public void init(long windowId, int windowWidth, int windowHeight, boolean debug) {
        log.debug("Initializing OpenGL context...");
        this.windowId = windowId;
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        GLCapabilities capabilities = createCapabilities();

        if(debug) {
            setupDebug(capabilities);
        }
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
        glfwSwapBuffers(windowId);
    }

    @Override
    public void windowResized(int newWidth, int newHeight) {
        glViewport(0, 0, newWidth, newHeight);
    }

    @Override
    public void dispose() {

    }

    private void setupDebug(GLCapabilities capabilities) {
        if (capabilities.OpenGL43) {
            log.debug("OpenGL 4.3 debugging enabled");
            GL43.glDebugMessageControl(
                    GL43.GL_DEBUG_SOURCE_API,
                    GL43.GL_DEBUG_TYPE_OTHER,
                    GL43.GL_DEBUG_SEVERITY_NOTIFICATION,
                    (IntBuffer) null,
                    false
            );
        }
        else if(capabilities.GL_KHR_debug){
            log.debug("KHR debugging enabled");
            KHRDebug.glDebugMessageControl(
                    KHRDebug.GL_DEBUG_SOURCE_API,
                    KHRDebug.GL_DEBUG_TYPE_OTHER,
                    KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
                    (IntBuffer)null,
                    false
            );
        }
        else if (capabilities.GL_ARB_debug_output) {
            log.debug("ARB debugging enabled");
            ARBDebugOutput.glDebugMessageControlARB(
                    ARBDebugOutput.GL_DEBUG_SOURCE_API_ARB,
                    ARBDebugOutput.GL_DEBUG_TYPE_OTHER_ARB,
                    ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB,
                    (IntBuffer) null,
                    false
            );
        }
    }
}
