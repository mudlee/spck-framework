package spck.core.renderer.backend.vulkan.init;

import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.VK10.*;
import static spck.core.renderer.backend.vulkan.VkErrorChecker.vkErrorCheck;

public class VkImageViewCreator {
  private static final Logger log = LoggerFactory.getLogger(VkImageViewCreator.class);

  public static void create(LogicalDevice logicalDevice, Swapchain swapchain){
    log.debug("Creating image views...");

    try(final var stack = stackPush()) {
      log.debug("* Querying final swap chain image count...");
      final var pImageCount = stack.callocInt(1);
      vkErrorCheck(vkGetSwapchainImagesKHR(logicalDevice.getDevice(), swapchain.getId(), pImageCount, null), "Failed to get swapchain images' count");
      log.debug("* > It's {}", pImageCount.get(0));

      final var swapchainImages = stack.callocLong(pImageCount.get(0));
      vkErrorCheck(vkGetSwapchainImagesKHR(logicalDevice.getDevice(), swapchain.getId(), pImageCount, swapchainImages), "Failed to get swapchain images' count");

      for(var i = 0; i<pImageCount.get(0); i++) {
        final var image = swapchainImages.get(i);
        log.debug("* > Creating image view #{} for image {}", i, image);
        final var createInfo = VkImageViewCreateInfo.callocStack(stack)
          .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
          .image(image)
          .viewType(VK_IMAGE_VIEW_TYPE_2D)
          .format(swapchain.getFormat().format())
          .components(it -> it
            .r(VK_COMPONENT_SWIZZLE_R)
            .g(VK_COMPONENT_SWIZZLE_G)
            .b(VK_COMPONENT_SWIZZLE_B)
            .a(VK_COMPONENT_SWIZZLE_A)
          )
          .subresourceRange(it -> it
            .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
            .baseMipLevel(0)
            .levelCount(1)
            .baseArrayLayer(0)
            .layerCount(1)
          );

        final var pImageView = stack.callocLong(1);
        // TODO: destroy with vkDestroyImageView();
        vkErrorCheck(vkCreateImageView(logicalDevice.getDevice(),createInfo,null,pImageView),"Failed to create image view");
        log.debug("* > Created: {}", pImageView.get(0));
      }
    }
  }
}
