package spck.core.renderer;

import spck.core.renderer.backend.RendererApi;
import spck.core.renderer.backend.opengl.OpenGLVertexBuffer;
import spck.core.renderer.backend.vulkan.VulkanVertexBuffer;

public abstract class VertexBuffer {
    public static VertexBuffer create(float[] vertices, VertexBufferLayout layout) {
        switch (RendererApi.backend) {
	        case OPENGL:
		        return new OpenGLVertexBuffer(vertices, layout);
	        case VULKAN:
		        return new VulkanVertexBuffer(vertices, layout);
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
