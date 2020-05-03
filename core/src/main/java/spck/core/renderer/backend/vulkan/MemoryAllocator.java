package spck.core.renderer.backend.vulkan;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.memUTF8;

public class MemoryAllocator {
	private static List<ByteBuffer> buffers = new ArrayList<>();

	public static ByteBuffer allocMemUTF8(String string) {
		ByteBuffer buffer = memUTF8(string);
		buffers.add(buffer);
		return buffer;
	}

	public static void dispose() {
		buffers.forEach(MemoryUtil::memFree);
	}
}
