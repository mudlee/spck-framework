package spck.core.renderer.backend.vulkan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class VkErrorChecker {
  private static final Logger log = LoggerFactory.getLogger(VkErrorChecker.class);

  public static void vkErrorCheck(int result, String message) {
    if(result!=VK_SUCCESS) {
      log.error(message);
      log.error(VulkanResult.translate(result));
      throw new RuntimeException(message);
    }
  }
}
