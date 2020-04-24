package spck.core.renderer.backend.bgfx;

import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.IndexBuffer;

import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.bgfx.BGFX.*;

public class BGFXIndexBuffer extends IndexBuffer {
    private static final Logger log = LoggerFactory.getLogger(BGFXIndexBuffer.class);
    private final ByteBuffer buffer;
    private final short bgfxBuffer;
    private final int numOfIndices;

    public BGFXIndexBuffer(int[] indices) {
        log.debug("Creating index buffer...");
        // allocating memory and creating the buffer
        numOfIndices = indices.length;
        buffer = MemoryUtil.memAlloc(indices.length*Short.BYTES);
        for(int vertex : indices){
            buffer.putShort((short)vertex);
        }

        if (buffer.remaining() != 0) {
            throw new RuntimeException(String.format("Buffer still has remaining space allocated: %d",buffer.remaining()));
        }

        buffer.flip();
        bgfxBuffer = bgfx_create_index_buffer(Objects.requireNonNull(bgfx_make_ref(buffer)), BGFX_BUFFER_NONE);
        log.debug("Index buffer has been created. Number of indices: {}",numOfIndices);
    }

    @Override
    public void bind() {
        bgfx_set_index_buffer(bgfxBuffer, 0, numOfIndices);
    }

    @Override
    public void unbind() {

    }

    @Override
    public void dispose() {
        log.debug("Disposing index buffer...");
        MemoryUtil.memFree(buffer);
        bgfx_destroy_index_buffer(bgfxBuffer);
    }
}
