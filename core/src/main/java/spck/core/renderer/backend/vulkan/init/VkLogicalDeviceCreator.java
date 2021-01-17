package spck.core.renderer.backend.vulkan.init;

import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.backend.vulkan.MemoryAllocator;
import spck.core.renderer.backend.vulkan.PhysicalDevice;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static spck.core.renderer.backend.vulkan.VkErrorChecker.vkErrorCheck;

public class VkLogicalDeviceCreator {
  private static final Logger log = LoggerFactory.getLogger(VkLogicalDeviceCreator.class);

  public static LogicalDevice create(PhysicalDevice physicalDevice){
    try(final var stack = stackPush()) {
      log.debug("Creating logical device...");

      log.debug("* Creating graphics queue...");
      final var graphicsFamilyPrio = stack.callocFloat(1).put(1.0f);
      graphicsFamilyPrio.flip();
      final var graphicsQueueCreateInfo = VkDeviceQueueCreateInfo.callocStack(1, stack)
        .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
        .queueFamilyIndex(physicalDevice.getQueueFamilies().getGraphicsFamily())
        .pQueuePriorities(graphicsFamilyPrio);


      log.debug("* Creating presentation queue...");
      final var presentationFamilyPrio = stack.callocFloat(1).put(1.0f);
      presentationFamilyPrio.flip();
      final var presentationQueueCreateInfo = VkDeviceQueueCreateInfo.callocStack(1, stack)
        .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
        .queueFamilyIndex(physicalDevice.getQueueFamilies().getPresentationFamily())
        .pQueuePriorities(presentationFamilyPrio);

      if (physicalDevice.getQueueFamilies().areQueueFamiliesDifferent()) {
        log.debug("* Queue families are different, passing both to VkDeviceCreateInfo");
        // note: if families are the same, we have to pass the index only once.
        graphicsQueueCreateInfo.put(presentationQueueCreateInfo);
        graphicsQueueCreateInfo.flip();
      }

      final var extensions = stack.callocPointer(1);
      final var VK_KHR_SWAPCHAIN_EXTENSION = MemoryAllocator.memUTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
      extensions.put(VK_KHR_SWAPCHAIN_EXTENSION);
      extensions.flip();

      log.debug("* Creating device create info...");
      final var deviceCreateInfo = VkDeviceCreateInfo.callocStack(stack)
        .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
        .pNext(NULL)
        .ppEnabledExtensionNames(extensions)
        .pQueueCreateInfos(graphicsQueueCreateInfo);

      final var pDevice = stack.callocPointer(1);
      log.debug("* Creating device...");
      vkErrorCheck(vkCreateDevice(physicalDevice.getDevice(), deviceCreateInfo, null, pDevice), "Failed to create logical device");
      final var vkDevice = new VkDevice(pDevice.get(0), physicalDevice.getDevice(), deviceCreateInfo);

      log.debug("* Logical device created");
      return new LogicalDevice(vkDevice, physicalDevice.getQueueFamilies().getPresentationFamily());
    }
  }
}
