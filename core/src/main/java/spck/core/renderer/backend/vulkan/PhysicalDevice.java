package spck.core.renderer.backend.vulkan;

import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import spck.core.renderer.Disposable;

public class PhysicalDevice implements Disposable {
  private final int score;
  private final VkPhysicalDevice device;
  private final VkPhysicalDeviceProperties properties;
  private final PhysicalDeviceQueueFamilies queueFamilies;

  public PhysicalDevice(int score, VkPhysicalDevice device, VkPhysicalDeviceProperties properties, PhysicalDeviceQueueFamilies queueFamilies) {
    this.score = score;
    this.device = device;
    this.properties = properties;
    this.queueFamilies = queueFamilies;
  }

  @Override
  public void dispose() {
    properties.free();
    queueFamilies.dispose();
  }

  public int getScore() {
    return score;
  }

  public VkPhysicalDevice getDevice() {
    return device;
  }

  public VkPhysicalDeviceProperties getProperties() {
    return properties;
  }

  public PhysicalDeviceQueueFamilies getQueueFamilies() {
    return queueFamilies;
  }
}
