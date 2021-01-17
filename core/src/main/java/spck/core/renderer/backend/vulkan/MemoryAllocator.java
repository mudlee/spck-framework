package spck.core.renderer.backend.vulkan;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MemoryAllocator {
	private static final List<ByteBuffer> buffers = new ArrayList<>();

	public static ByteBuffer memUTF8(String string) {
		final var buffer = MemoryUtil.memUTF8(string);
		buffers.add(buffer);
		return buffer;
	}

	public static void dispose() {
		buffers.forEach(MemoryUtil::memFree);
	}
}
