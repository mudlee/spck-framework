package spck.core.renderer.backend.bgfx;

import org.lwjgl.bgfx.BGFXVertexLayout;
import spck.core.renderer.IndexBuffer;
import spck.core.renderer.VertexArray;
import spck.core.renderer.VertexBuffer;
import spck.core.renderer.VertexLayoutAttribute;

import java.util.ArrayList;
import java.util.List;

public class BGFXVertexArray extends VertexArray {

    private final List<VertexBuffer> vertexBuffers = new ArrayList<>();
    private IndexBuffer indexBuffer;

    public BGFXVertexArray() {

    }

    @Override
    public void bind() {
        for (VertexBuffer vertexBuffer : vertexBuffers) {
            vertexBuffer.bind();
        }

        indexBuffer.bind();
    }

    @Override
    public void unbind() {

    }

    @Override
    public void addVertexBuffer(VertexBuffer buffer) {
        //buffer.bind(); TODO

        vertexBuffers.add(buffer);
    }

    @Override
    public void setIndexBuffer(IndexBuffer buffer) {
        //buffer.bind(); TODO
        indexBuffer = buffer;
    }

    @Override
    public void dispose() {
        vertexBuffers.forEach(VertexBuffer::dispose);
        indexBuffer.dispose();
    }
}
