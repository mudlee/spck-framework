package spck.core.renderer;

import spck.core.renderer.backend.RendererApi;
import spck.core.renderer.backend.opengl.OpenGLVertexArray;
import spck.core.renderer.backend.vulkan.VulkanVertexArray;

import java.util.List;
import java.util.Optional;

public abstract class VertexArray {
    public static VertexArray create(){
        switch (RendererApi.backend) {
            case OPENGL:
                return new OpenGLVertexArray();
            case VULKAN:
                return new VulkanVertexArray();
            default:
                throw new UnsupportedOperationException();
        }
    }

    public abstract void bind();

    public abstract void unbind();

    public abstract void addVertexBuffer(VertexBuffer buffer);

    public abstract void setIndexBuffer(IndexBuffer indexBuffer);

    public abstract List<VertexBuffer> getVertexBuffers();

    public abstract Optional<IndexBuffer> getIndexBuffer();

    public abstract void dispose();
}
