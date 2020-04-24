package spck.core.renderer;

import spck.core.renderer.backend.bgfx.BGFXVertexBuffer;

public abstract class VertexBuffer {
    public static VertexBuffer create(float[] vertices, VertexBufferLayout layout) {
        switch (Renderer.API) {
            case BGFX:
                return new BGFXVertexBuffer(vertices, layout);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public abstract void bind();

    public abstract void unbind();

    public abstract void dispose();
}
