package spck.core.renderer;

import spck.core.renderer.backend.RendererApi;
import spck.core.renderer.backend.opengl.OpenGLIndexBuffer;

public abstract class IndexBuffer {
    public static IndexBuffer create(int[] indices) {
        switch (RendererApi.backend) {
            case OPENGL:
                return new OpenGLIndexBuffer(indices);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public abstract int getId();

    public abstract int getLength();

    public abstract void bind();

    public abstract void unbind();

    public abstract void dispose();
}
