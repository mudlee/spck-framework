package spck.core.renderer.backend.opengl;

import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.io.ResourceLoader;
import spck.core.renderer.Shader;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL41.*;

public class OpenGLShader extends Shader {
    private static final Logger log = LoggerFactory.getLogger(OpenGLShader.class);
    private final int programId;

    public OpenGLShader(String vertexShaderName, String fragmentShaderName) {
        log.debug("Creating shader program for with vertex shader '{}' and fragment shader '{}'", vertexShaderName, fragmentShaderName);
        programId = glCreateProgram();

        String vertPath = String.format("/shaders/%s.glsl", vertexShaderName);
        String fragPath = String.format("/shaders/%s.glsl", fragmentShaderName);

        int vertexId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexId, ResourceLoader.load(vertPath));
        glCompileShader(vertexId);
        validateShader(vertexId, vertPath);
        log.debug(" - vertex shader compiled {}", vertexId);

        int fragmentId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentId, ResourceLoader.load(fragPath));
        glCompileShader(fragmentId);
        validateShader(fragmentId, fragPath);
        log.debug(" - fragment shader compiled {}", fragmentId);

        glAttachShader(programId, vertexId);
        glAttachShader(programId, fragmentId);
        glLinkProgram(programId);
        validateShaderProgram(vertPath);
        glDetachShader(programId, vertexId);
        glDetachShader(programId, fragmentId);
        log.debug(" - shaders linked to program");
    }

    @Override
    public int getProgram() {
        return programId;
    }

    @Override
    public void bind() {
        log.trace("Bind shader program {}", programId);
        glUseProgram(programId);
    }

    @Override
    public void unbind() {
        log.trace("Unbind shader program {}", programId);
        glUseProgram(0);
    }

    @Override
    public void dispose() {
        log.trace("Dispose shader program {}", programId);
        unbind();

        glDeleteProgramPipelines(programId);
    }

    private void validateShader(int shaderId, String path) {
        if(glGetShaderi(shaderId,GL_COMPILE_STATUS) == GL_TRUE) {
            return;
        }

        log.error(
                "Shader '{}' could not be compiled\n---\n{}---",
                path,
                glGetShaderInfoLog(shaderId, 1024)
        );
        throw new RuntimeException("Shader validating failed");
    }

    private void validateShaderProgram(String path) {
        log.debug("Validating shader {}",path);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(1);
            glGetProgramiv(programId, GL_LINK_STATUS, buffer);
            if (buffer.get() == GL_FALSE) {
                log.error(
                        "Shader '{}' could not be linked\n---\n{}---",
                        path,
                        glGetProgramInfoLog(programId, 1024)
                );
                throw new RuntimeException("Shader validating failed");
            }
        }
    }
}
