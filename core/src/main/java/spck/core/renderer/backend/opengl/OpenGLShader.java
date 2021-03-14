package spck.core.renderer.backend.opengl;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.io.ResourceLoader;
import spck.core.renderer.Shader;
import spck.core.renderer.backend.SPIRVCompiler;

import static org.lwjgl.opengl.ARBGLSPIRV.GL_SHADER_BINARY_FORMAT_SPIR_V_ARB;
import static org.lwjgl.opengl.ARBGLSPIRV.glSpecializeShaderARB;
import static org.lwjgl.opengl.GL41.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memCalloc;

public class OpenGLShader extends Shader {
    private static final Logger log = LoggerFactory.getLogger(OpenGLShader.class);
    private final int programId;

    public OpenGLShader(String vertexShaderName, String fragmentShaderName) {
        log.debug("Creating shader program for with vertex shader '{}' and fragment shader '{}'", vertexShaderName, fragmentShaderName);
        programId = glCreateProgram();

        String vertPath = String.format("/shaders/%s.glsl", vertexShaderName);
        String fragPath = String.format("/shaders/%s.glsl", fragmentShaderName);

        final int vertexId = constructShader(vertexShaderName, vertPath, ShaderType.VERTEX);
        log.debug(" * vertex shader compiled {}", vertexId);
        final int fragmentId = constructShader(fragmentShaderName, fragPath, ShaderType.FRAGMENT);
        log.debug(" * fragment shader compiled {}", fragmentId);

        glAttachShader(programId, vertexId);
        glAttachShader(programId, fragmentId);
        glLinkProgram(programId);
        validateShaderProgram(vertPath);
        glDetachShader(programId, vertexId);
        glDetachShader(programId, fragmentId);
        log.debug(" * shaders linked to program");
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

    private int constructShader(String shaderName, String path, ShaderType type) {
        try(final var stack = stackPush()) {
            final var spirv = SPIRVCompiler.compile(
              ResourceLoader.loadToByteBuffer(path, stack),
              type.shadercCode,
              shaderName
            );

            final var id = glCreateShader(type.glCode);
            final var idPtr = stack.callocInt(1).put(id).flip();
            final var spirvBuf = memCalloc(spirv.getBytes().length).put(spirv.getBytes()).flip();

            glShaderBinary(idPtr, GL_SHADER_BINARY_FORMAT_SPIR_V_ARB, spirvBuf);
            glSpecializeShaderARB(id, stack.UTF8("main"), new int[]{}, new int[]{});
            validateShader(id, path);

            return id;
        }
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
        try (MemoryStack stack = stackPush()) {
            final var buffer = stack.callocInt(1);
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

    private enum ShaderType {
        VERTEX(Shaderc.shaderc_vertex_shader, GL_VERTEX_SHADER),
        FRAGMENT(Shaderc.shaderc_fragment_shader, GL_FRAGMENT_SHADER);

        private final int shadercCode;
        private final int glCode;

        ShaderType(int shadercCode, int glCode) {
            this.shadercCode = shadercCode;
            this.glCode = glCode;
        }
    }
}
