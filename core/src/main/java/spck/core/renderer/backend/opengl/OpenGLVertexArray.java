package spck.core.renderer.backend.opengl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.IndexBuffer;
import spck.core.renderer.VertexArray;
import spck.core.renderer.VertexBuffer;
import spck.core.renderer.VertexLayoutAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.opengl.GL41.*;

public class OpenGLVertexArray extends VertexArray {
	private static final Logger log = LoggerFactory.getLogger(OpenGLVertexArray.class);
	private final int id;
	private final List<VertexBuffer> vertexBuffers = new ArrayList<>();
	private IndexBuffer indexBuffer;

	public OpenGLVertexArray() {
		id = glGenVertexArrays();
		log.debug("VertexArray created {}", id);
	}

	@Override
	public void bind() {
		log.trace("Bind vertex array {}", id);
		glBindVertexArray(id);
	}

	@Override
	public void unbind() {
		log.trace("Unbind vertex array {}", id);
		glBindVertexArray(0);
	}

	@Override
	public void addVertexBuffer(VertexBuffer buffer) {
		log.trace("Add vertex buffer {} to vertex array {}", buffer.getId(), id);
		bind();
		if (buffer.getLayout() == null) {
			throw new RuntimeException("VertexBuffer does not define its layout");
		}

		buffer.bind();
		int index = 0;
		for (VertexLayoutAttribute attribute : buffer.getLayout().getAttributes()) {
			glEnableVertexAttribArray(index);
			glVertexAttribPointer(
					attribute.getPosition(),
					attribute.getDataSize(),
					attribute.getDataType(),
					attribute.isNormalized(),
					attribute.getStride(),
					attribute.getOffset()
			);
			index++;
		}
		buffer.unbind();
		unbind();
	}

	@Override
	public void setIndexBuffer(IndexBuffer buffer) {
		log.trace("Add index buffer {} to vertex array {}", buffer.getId(), id);
		bind();
		buffer.bind();
		unbind();
		this.indexBuffer = buffer;
	}

	@Override
	public List<VertexBuffer> getVertexBuffers() {
		return vertexBuffers;
	}

	@Override
	public Optional<IndexBuffer> getIndexBuffer() {
		return Optional.ofNullable(indexBuffer);
	}

	@Override
	public void dispose() {
		log.trace("Dispose vertex array {}", id);
		glDeleteVertexArrays(id);
	}
}
