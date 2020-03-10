package spck.core.render.bgfx;

import org.lwjgl.bgfx.BGFXMemory;
import org.lwjgl.bgfx.BGFXVertexLayout;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.bgfx.BGFX.*;
import static org.lwjgl.bgfx.BGFX.BGFX_BUFFER_NONE;

public class VertexBuffer extends BgfxBuffer {
	public VertexBuffer(float[] vertices, BGFXVertexLayout layout) {
		buffer = MemoryUtil.memAlloc(vertices.length*Float.BYTES);

		for(float vertex : vertices){
			buffer.putFloat(vertex);
		}

		if (buffer.remaining() != 0) {
			throw new RuntimeException(String.format("Buffer still has remaining space allocated: %d",buffer.remaining()));
		}

		buffer.flip();

		BGFXMemory vbhMem = bgfx_make_ref(buffer);
		bgfxBuffer = bgfx_create_vertex_buffer(vbhMem, layout, BGFX_BUFFER_NONE);
	}

	public short getBgfxBuffer() {
		return bgfxBuffer;
	}

	@Override
	public void dispose() {
		MemoryUtil.memFree(buffer);
		bgfx_destroy_vertex_buffer(bgfxBuffer);
	}
}
