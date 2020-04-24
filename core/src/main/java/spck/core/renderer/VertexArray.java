package spck.core.renderer;

import spck.core.renderer.backend.bgfx.BGFXVertexArray;

public abstract class VertexArray {
    public static VertexArray create(){
        switch (Renderer.API) {
            case BGFX:
                return new BGFXVertexArray();
            default:
                throw new UnsupportedOperationException();
        }
    }

    public abstract void bind();

    public abstract void unbind();

    public abstract void addVertexBuffer(VertexBuffer buffer);

    public abstract void setIndexBuffer(IndexBuffer indexBuffer);

    public abstract void dispose();
}
