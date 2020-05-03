package spck.core.renderer.backend.vulkan;

import spck.core.renderer.IndexBuffer;
import spck.core.renderer.VertexArray;
import spck.core.renderer.VertexBuffer;

import java.util.List;
import java.util.Optional;

public class VulkanVertexArray extends VertexArray {
	@Override
	public void bind() {

	}

	@Override
	public void unbind() {

	}

	@Override
	public void addVertexBuffer(VertexBuffer buffer) {

	}

	@Override
	public void setIndexBuffer(IndexBuffer indexBuffer) {

	}

	@Override
	public List<VertexBuffer> getVertexBuffers() {
		return null;
	}

	@Override
	public Optional<IndexBuffer> getIndexBuffer() {
		return Optional.empty();
	}

	@Override
	public void dispose() {

	}
}
