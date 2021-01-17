package spck.core.renderer.backend.opengl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.IndexBuffer;

import static org.lwjgl.opengl.GL41.*;
import static org.lwjgl.system.MemoryStack.stackPush;

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
        try(final var stack = stackPush()) {
            this.length = indices.length;
            id = glGenBuffers();
            final var buffer = stack.callocInt(indices.length).put(indices).flip();
            bind();
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
            log.debug("IndexBuffer created {}", id);
        }
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
