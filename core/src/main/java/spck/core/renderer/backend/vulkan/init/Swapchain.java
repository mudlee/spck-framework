package spck.core.renderer.backend.vulkan.init;

import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import spck.core.renderer.Disposable;

import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;

public class Swapchain implements Disposable {
  private final long id;
  private final LogicalDevice logicalDevice;
  private final VkSurfaceFormatKHR format;
  private final VkExtent2D extent;

  public Swapchain(long id, LogicalDevice logicalDevice, VkSurfaceFormatKHR format, VkExtent2D extent) {
    this.id = id;
    this.logicalDevice = logicalDevice;
    this.format = format;
    this.extent = extent;
  }

  @Override
  public void dispose() {
    vkDestroySwapchainKHR(logicalDevice.getDevice(), id, null);
  }

  public long getId() {
    return id;
  }

  public VkSurfaceFormatKHR getFormat() {
    return format;
  }

  public VkExtent2D getExtent() {
    return extent;
  }
}
