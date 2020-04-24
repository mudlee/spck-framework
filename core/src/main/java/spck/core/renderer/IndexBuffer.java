package spck.core.renderer;

import spck.core.renderer.backend.bgfx.BGFXIndexBuffer;

public abstract class IndexBuffer {
    public static IndexBuffer create(int[] indices) {
        switch (Renderer.API) {
            case BGFX:
                return new BGFXIndexBuffer(indices);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public abstract void bind();

    public abstract void unbind();

    public abstract void dispose();
}
