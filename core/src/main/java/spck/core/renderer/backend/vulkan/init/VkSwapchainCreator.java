package spck.core.renderer.backend.vulkan.init;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.backend.vulkan.PhysicalDevice;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memCallocInt;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static spck.core.renderer.backend.vulkan.VkErrorChecker.vkErrorCheck;

public class VkSwapchainCreator {
  private static final Logger log = LoggerFactory.getLogger(VkSwapchainCreator.class);
  public static Swapchain swapchain; // TODO

  public static Swapchain create(PhysicalDevice physicalDevice, LogicalDevice logicalDevice, long surface, long windowId) {
    log.debug("Creating swap chain...");
    final var swapchainDetails = querySwapchainSupport(physicalDevice.getDevice(), surface);

    if(isSwapchainAdequate(swapchainDetails)){
      return createSwapchain(physicalDevice, logicalDevice, surface, swapchainDetails, windowId);
    }
    else {
      throw new RuntimeException("Swap chain is not adequate");
    }
  }

  private static Swapchain createSwapchain(
    PhysicalDevice physicalDevice,
    LogicalDevice logicalDevice,
    long surface,
    SwapchainSupportDetails swapchainDetails,
    long windowId
  ) {
    try(final var stack = stackPush()) {
      final var surfaceFormat = selectSurfaceFormat(swapchainDetails.getFormats());
      final var presentMode = selectPresentMode(swapchainDetails.getPresentModes());
      final var extent = selectSwapExtent(swapchainDetails.getCapabilities(), windowId, stack);
      // Sometimes have to wait on the driver to complete internal operations before we can acquire another image to render to.
      // Therefore it is recommended to request at least one more image than the minimum.
      var imageCount = swapchainDetails.getCapabilities().minImageCount() + 1;

      // But we can't exceed the chain's maximum
      final var maxImageCount = swapchainDetails.getCapabilities().maxImageCount();
      if (maxImageCount > 0 && imageCount > maxImageCount) {
        imageCount = maxImageCount;
      }

      log.debug("* Preparing...");
      VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.callocStack(stack)
        .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
        .surface(surface)
        .minImageCount(imageCount)
        .imageFormat(surfaceFormat.format())
        .imageColorSpace(surfaceFormat.colorSpace())
        .imageExtent(extent)
        .imageArrayLayers(1)
        .preTransform(swapchainDetails.getCapabilities().currentTransform())
        .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
        .presentMode(presentMode)
        .clipped(true)
        .oldSwapchain(VK_NULL_HANDLE)
        .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

      if (physicalDevice.getQueueFamilies().areQueueFamiliesDifferent()) {
        log.debug("* > Image sharing mode: VK_SHARING_MODE_CONCURRENT");
        createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
        final var graphicsFamily = stack.callocInt(1);
        graphicsFamily.put(physicalDevice.getQueueFamilies().getGraphicsFamily());
        createInfo.pQueueFamilyIndices(graphicsFamily);
      } else {
        log.debug("* > Image sharing mode: VK_SHARING_MODE_EXCLUSIVE");
        createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
      }

      log.debug("* Creating...");
      final var pSwapchain = stack.callocLong(1);
      vkErrorCheck(vkCreateSwapchainKHR(logicalDevice.getDevice(), createInfo, null, pSwapchain), "Failed to create swap chain");

      log.debug("* Swap chain created: {}", pSwapchain.get(0));

      swapchain=new Swapchain(pSwapchain.get(0), logicalDevice, surfaceFormat, extent);
      return swapchain;
    }
  }

  private static boolean isSwapchainAdequate(SwapchainSupportDetails swapchainSupportDetails) {
    final var adequate = swapchainSupportDetails.getFormats().hasRemaining() && swapchainSupportDetails.getPresentModes().hasRemaining();
    log.debug("* > Swap chain is adequate: {}", adequate);
    return adequate;
  }

  private static SwapchainSupportDetails querySwapchainSupport(VkPhysicalDevice device, long surface) {
    log.debug("* > Querying swap chain support...");
    final var capabilities = VkSurfaceCapabilitiesKHR.calloc();
    vkErrorCheck(vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, capabilities), "Failed to get surface capabilities");

    final var pSurfaceFormatCount = memCallocInt(1);
    vkErrorCheck(vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface,pSurfaceFormatCount,null), "Failed to get surface format's count");

    final var surfaceFormats = VkSurfaceFormatKHR.calloc(pSurfaceFormatCount.get(0));
    vkErrorCheck(vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface,pSurfaceFormatCount,surfaceFormats), "Failed to get surface formats");

    final var pPresentModeCount = memCallocInt(1);
    vkErrorCheck(vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, pPresentModeCount, null), "Failed to get surface present modes's count");

    final var presentModes = memCallocInt(pPresentModeCount.get(0));
    vkErrorCheck(vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, pPresentModeCount, presentModes), "Failed to get surface present modes");

    return new SwapchainSupportDetails(capabilities, surfaceFormats, presentModes);
  }

  private static VkSurfaceFormatKHR selectSurfaceFormat(VkSurfaceFormatKHR.Buffer formats) {
    log.debug("* > Selecting surface format...");
    for (VkSurfaceFormatKHR format : formats) {
      if(format.format() == VK_FORMAT_B8G8R8A8_SRGB && format.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
        log.debug("* > Found one has format=VK_FORMAT_B8G8R8A8_SRGB and colorSpace=VK_COLOR_SPACE_SRGB_NONLINEAR_KHR");
        return format;
      }
    }

    log.debug("* > !!! Could not find ideal format, using the first one");
    return formats.get(0);
  }

  private static int selectPresentMode(IntBuffer modes) {
    log.debug("* > Selecting present mode...");

    while (modes.hasRemaining()) {
      if(modes.get() == VK_PRESENT_MODE_MAILBOX_KHR) {
        log.debug("* > Using VK_PRESENT_MODE_MAILBOX_KHR");
        return VK_PRESENT_MODE_MAILBOX_KHR;
      }
    }

    log.debug("* > Using VK_PRESENT_MODE_FIFO_KHR");
    return VK_PRESENT_MODE_FIFO_KHR;
  }

  private static VkExtent2D selectSwapExtent(VkSurfaceCapabilitiesKHR capabilities, long windowId, MemoryStack stack) {
    log.debug("* > Selecting swap extent...");
    if(capabilities.currentExtent().width() != 0xFFFFFFFF) {
      log.debug("* > Using current extent");
      return capabilities.currentExtent();
    }

    final var w = stack.callocInt(1);
    final var h = stack.callocInt(1);

    glfwGetFramebufferSize(windowId, w, h);
    final var extent = VkExtent2D.callocStack(stack);
    extent.set(w.get(), h.get());

    final var width = Math.max(capabilities.minImageExtent().width(), Math.min(capabilities.maxImageExtent().width(), extent.width()));
    final var height = Math.max(capabilities.minImageExtent().height(), Math.min(capabilities.maxImageExtent().height(), extent.height()));
    extent.width(width);
    extent.height(height);

    log.debug("* > Using {}x{}", width, height);
    return extent;
  }
}
