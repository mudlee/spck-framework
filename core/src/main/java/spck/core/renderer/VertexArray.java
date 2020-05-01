package spck.core.renderer;

import spck.core.renderer.backend.RendererApi;
import spck.core.renderer.backend.opengl.OpenGLVertexArray;

public abstract class VertexArray {
    public static VertexArray create(){
        switch (RendererApi.backend) {
            case OPENGL:
                return new OpenGLVertexArray();
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
