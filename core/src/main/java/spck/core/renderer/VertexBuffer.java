package spck.core.renderer;

import spck.core.renderer.backend.opengl.OpenGLVertexBuffer;

public abstract class VertexBuffer extends GraphicsData{
    public static VertexBuffer create(float[] vertices, VertexBufferLayout layout) {
        switch (backend) {
	        case OPENGL:
		        return new OpenGLVertexBuffer(vertices, layout);
	        default:
		        throw new UnsupportedOperationException();
        }
    }

    public abstract int getId();

    public abstract int getLength();

    public abstract VertexBufferLayout getLayout();

    public abstract void bind();

    public abstract void unbind();

    public abstract void dispose();
}
