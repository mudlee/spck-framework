package spck.core.renderer.backend.vulkan;

import spck.core.renderer.VertexBuffer;
import spck.core.renderer.VertexBufferLayout;

public class VulkanVertexBuffer extends VertexBuffer {
	public VulkanVertexBuffer(float[] vertices, VertexBufferLayout layout) {

	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public VertexBufferLayout getLayout() {
		return null;
	}

	@Override
	public void bind() {

	}

	@Override
	public void unbind() {

	}

	@Override
	public void dispose() {

	}
}
