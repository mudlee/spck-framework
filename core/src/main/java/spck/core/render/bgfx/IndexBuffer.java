package spck.core.render.bgfx;

import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.bgfx.BGFX.*;

public class IndexBuffer extends BgfxBuffer {
	public IndexBuffer(int[] indices) {
		buffer = MemoryUtil.memAlloc(indices.length*Short.BYTES);
		for(int vertex : indices){
			buffer.putShort((short)vertex);
		}

		if (buffer.remaining() != 0) {
			throw new RuntimeException(String.format("Buffer still has remaining space allocated: %d",buffer.remaining()));
		}

		buffer.flip();
		bgfxBuffer = bgfx_create_index_buffer(bgfx_make_ref(buffer), BGFX_BUFFER_NONE);
	}

	public short getBgfxBuffer() {
		return bgfxBuffer;
	}

	@Override
	public void dispose() {
		MemoryUtil.memFree(buffer);
		bgfx_destroy_index_buffer(bgfxBuffer);
	}
}
