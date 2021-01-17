package spck.core.renderer.backend.vulkan.init;

import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import spck.core.renderer.Disposable;

import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryUtil.memFree;

public class SwapchainSupportDetails implements Disposable {
  private final VkSurfaceCapabilitiesKHR capabilities;
  private final VkSurfaceFormatKHR.Buffer formats;
  private final IntBuffer presentModes;

  public SwapchainSupportDetails(VkSurfaceCapabilitiesKHR capabilities, VkSurfaceFormatKHR.Buffer formats, IntBuffer presentModes) {
    this.capabilities = capabilities;
    this.formats = formats;
    this.presentModes = presentModes;
  }

  @Override
  public void dispose() {
    capabilities.free();
    formats.free();
    memFree(presentModes);
  }

  public VkSurfaceCapabilitiesKHR getCapabilities() {
    return capabilities;
  }

  public VkSurfaceFormatKHR.Buffer getFormats() {
    return formats;
  }

  public IntBuffer getPresentModes() {
    return presentModes;
  }
}
