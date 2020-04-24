package spck.core.renderer.backend.bgfx;

import org.lwjgl.bgfx.BGFXVertexLayout;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.VertexBuffer;
import spck.core.renderer.VertexBufferLayout;
import spck.core.renderer.VertexLayoutAttribute;

import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.bgfx.BGFX.*;

public class BGFXVertexBuffer extends VertexBuffer {
    private static final Logger log = LoggerFactory.getLogger(BGFXVertexBuffer.class);
    private final BGFXVertexLayout bgfxVertexLayout;
    private final ByteBuffer buffer;
    private final short bgfxBuffer;
    private final int numOfVertices;

    public BGFXVertexBuffer(float[] vertices, VertexBufferLayout layout) {
        log.debug("Creating vertex buffer...");
        log.debug("Constructing vertex buffer layout...");

        bgfxVertexLayout = BGFXVertexLayout.calloc();

        bgfx_vertex_layout_begin(bgfxVertexLayout, BGFXContext.renderer);
        for (VertexLayoutAttribute attribute : layout.getAttributes()) {
            bgfx_vertex_layout_add(
                    bgfxVertexLayout,
                    attribute.getPosition(),
                    attribute.getDataSize(),
                    attribute.getDataType(),
                    attribute.isNormalized(),
                    false
            );
        }
        bgfx_vertex_layout_end(bgfxVertexLayout);

        // allocating memory and creating the buffer
        numOfVertices = vertices.length / 4;
        buffer = MemoryUtil.memAlloc(vertices.length * Float.BYTES);

        for(float vertex : vertices){
            buffer.putFloat(vertex);
        }

        if (buffer.remaining() != 0) {
            throw new RuntimeException(String.format("Buffer still has remaining space allocated: %d",buffer.remaining()));
        }

        buffer.flip();
        bgfxBuffer = bgfx_create_vertex_buffer(Objects.requireNonNull(bgfx_make_ref(buffer)), bgfxVertexLayout, BGFX_BUFFER_NONE);
        log.debug("Vertex buffer has been created. Number of vertices: {}",numOfVertices);
    }

    @Override
    public void bind() {
        bgfx_set_vertex_buffer( 0, bgfxBuffer, 0, numOfVertices);
    }

    @Override
    public void unbind() {
    }

    @Override
    public void dispose() {
        log.debug("Disposing vertex buffer...");
        bgfxVertexLayout.free();
        MemoryUtil.memFree(buffer);
        bgfx_destroy_vertex_buffer(bgfxBuffer);
    }
}
