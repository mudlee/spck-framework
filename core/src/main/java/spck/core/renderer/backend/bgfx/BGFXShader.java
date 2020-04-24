package spck.core.renderer.backend.bgfx;

import org.lwjgl.bgfx.BGFXReleaseFunctionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.io.ResourceLoader;
import spck.core.renderer.Shader;

import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.bgfx.BGFX.*;
import static org.lwjgl.bgfx.BGFX.bgfx_make_ref_release;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.nmemFree;

public class BGFXShader extends Shader {
    private static final BGFXReleaseFunctionCallback releaseMemoryCb = BGFXReleaseFunctionCallback.create((_ptr, _userData) -> nmemFree(_ptr));
    private static final Logger log = LoggerFactory.getLogger(BGFXShader.class);
    private final short program;

    public BGFXShader(String vertexShaderName, String fragmentShaderName) {
        log.debug("Creating shader with vertex shader: '{}', fragment shader: '{}'...", vertexShaderName, fragmentShaderName);
        short vertexShaderId = load(vertexShaderName);
        short fragmentShaderId = load(fragmentShaderName);
        program = bgfx_create_program(vertexShaderId, fragmentShaderId, true);
        log.debug("Vertex shader has been created");
    }

    @Override
    public short getProgram() {
        return program;
    }

    @Override
    public void dispose() {
        bgfx_destroy_program(program);
    }

    private short load(String name){
        String resourcePath = "/shaders/";

        switch (BGFXContext.renderer) {
            case BGFX_RENDERER_TYPE_DIRECT3D11:
            case BGFX_RENDERER_TYPE_DIRECT3D12:
                resourcePath += "dx11/";
                break;

            case BGFX_RENDERER_TYPE_DIRECT3D9:
                resourcePath += "dx9/";
                break;

            case BGFX_RENDERER_TYPE_OPENGL:
                resourcePath += "glsl/";
                break;

            case BGFX_RENDERER_TYPE_METAL:
                resourcePath += "metal/";
                break;
            default:
                throw new RuntimeException("No demo shaders supported for " + bgfx_get_renderer_name(BGFXContext.renderer) + " renderer");
        }

        resourcePath += name;
        resourcePath += ".bin";

        ByteBuffer shaderCode = ResourceLoader.load(resourcePath);

        return bgfx_create_shader(Objects.requireNonNull(bgfx_make_ref_release(shaderCode, releaseMemoryCb, NULL)));
    }
}
