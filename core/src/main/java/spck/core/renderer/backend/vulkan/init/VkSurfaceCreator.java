package spck.core.renderer.backend.vulkan.init;

import org.lwjgl.vulkan.VkInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryStack.stackPush;
import static spck.core.renderer.backend.vulkan.VkErrorChecker.vkErrorCheck;

public class VkSurfaceCreator {
  private static final Logger log = LoggerFactory.getLogger(VkSurfaceCreator.class);

  public static long create(VkInstance vkInstance, long windowId) {
    try(final var stack = stackPush()) {
      log.debug("Creating Window Surface...");
      final var pSurface = stack.callocLong(1);
      vkErrorCheck(glfwCreateWindowSurface(vkInstance, windowId, null, pSurface), "Failed to create surface");

      final long surface = pSurface.get(0);

      log.debug("Surface {} created, cleaning up...", surface);
      return surface;
    }
  }
}
