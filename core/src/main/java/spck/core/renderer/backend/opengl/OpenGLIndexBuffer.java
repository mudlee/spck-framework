package spck.core.renderer.backend.opengl;

import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.IndexBuffer;

import java.nio.Buffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL41.*;

/**
 * An OpenGL implementation of IndexBuffer
 *
 * Don't unbind before unbinding VAO, because it's state is not saved
 * VBOs' state is saved because of the call on glVertexAttribPointer
 */
public class OpenGLIndexBuffer extends IndexBuffer {
    private static final Logger log = LoggerFactory.getLogger(OpenGLIndexBuffer.class);
    private final int id;
    private final int length;

    public OpenGLIndexBuffer(int[] indices) {
        this.length = indices.length;
        id = glGenBuffers();
        IntBuffer buffer = (IntBuffer) ((Buffer) MemoryUtil.memAllocInt(indices.length).put(indices)).flip();
        bind();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW);
        MemoryUtil.memFree(buffer);
        log.debug("IndexBuffer created {}", id);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public void bind() {
        log.trace("Bind index buffer {}", id);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
    }

    @Override
    public void unbind() {
        log.trace("Unbind index buffer {}", id);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void dispose() {
        log.trace("Dispose index buffer {}", id);
        glDeleteBuffers(id);
    }
}
