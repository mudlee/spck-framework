package spck.core.renderer;

import spck.core.renderer.backend.bgfx.BGFXShader;

public abstract class Shader {
    public static Shader create(String vertexShaderName, String fragmentShaderName){
        switch (Renderer.API) {
            case BGFX:
                return new BGFXShader(vertexShaderName, fragmentShaderName);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public abstract short getProgram();

    public abstract void dispose();
}
