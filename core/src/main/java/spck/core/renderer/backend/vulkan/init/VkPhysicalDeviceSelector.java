package spck.core.renderer.backend.vulkan.init;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.backend.vulkan.PhysicalDevice;
import spck.core.renderer.backend.vulkan.PhysicalDeviceQueueFamilies;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.TreeMap;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static spck.core.renderer.backend.vulkan.VkErrorChecker.vkErrorCheck;

public class VkPhysicalDeviceSelector {
  private static final Logger log = LoggerFactory.getLogger(VkPhysicalDeviceSelector.class);
  private static final String[] REQUIRED_DEVICE_EXTENSIONS = {VK_KHR_SWAPCHAIN_EXTENSION_NAME};

  public static PhysicalDevice select(VkInstance vkInstance, long surface){
    try(final var stack = stackPush()) {
      log.debug("Selecting physical device...");
      final var deviceCandidates = new TreeMap<Integer, PhysicalDevice>();

      final var pDeviceCount = stack.callocInt(1);
      vkErrorCheck(vkEnumeratePhysicalDevices(vkInstance, pDeviceCount, null), "Failed to find physical with Vulkan support");

      final var physicalDevices = stack.callocPointer(pDeviceCount.get(0));
      vkErrorCheck(vkEnumeratePhysicalDevices(vkInstance, pDeviceCount, physicalDevices), "Failed to get physical devices");

      log.debug("Looking for the devices...");
      for (int deviceIndex = 0; deviceIndex < pDeviceCount.get(0); deviceIndex++) {
        final var pDevice = physicalDevices.get(deviceIndex);
        final var device = new VkPhysicalDevice(pDevice, vkInstance);
        final var candidate = rateDeviceSuitability(device, surface, stack);
        deviceCandidates.put(candidate.getScore(), candidate);
      }

      if (deviceCandidates.isEmpty()) {
        throw new RuntimeException("Could not find a device that supports Vulkan");
      }

      final var selectedDevice = deviceCandidates.lastEntry();
      log.debug("* Selected device: {}", selectedDevice.getValue().getProperties().deviceNameString());
      log.debug("* > graphics queue family index: {}", selectedDevice.getValue().getQueueFamilies().getGraphicsFamily());
      log.debug("* > presentation queue family index: {}", selectedDevice.getValue().getQueueFamilies().getPresentationFamily());

      if (selectedDevice.getValue().getScore() == 0) {
        log.error("#######################");
        log.error("ALL DEVICES GOT 0 SCORE");
        log.error("#######################");
        throw new RuntimeException();
      }

      // note: selected device's properties will be freed when the app shuts down
      deviceCandidates
        .values()
        .stream()
        .filter(d -> d.getDevice().address() != selectedDevice.getValue().getDevice().address())
        .forEach(c -> c.getProperties().free());

      return selectedDevice.getValue();
    }
  }

  private static PhysicalDevice rateDeviceSuitability(VkPhysicalDevice device, long surface, MemoryStack stack) {
    int score = 0;

    // note: properties will be freed manually when the device is destroyed
    final var deviceProperties = VkPhysicalDeviceProperties.calloc();
    vkGetPhysicalDeviceProperties(device, deviceProperties);
    log.debug("* Found device: {}", deviceProperties.deviceNameString());

    if (deviceProperties.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
      log.debug("* > device is a discrete GPU");
      score += 1000;
    } else if (deviceProperties.deviceType() == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU) {
      log.debug("* > device is an integrated GPU");
      score += 500;
    }

    score += deviceProperties.limits().maxImageDimension2D();
    score += deviceProperties.limits().maxImageDimension3D();

    final var deviceFeatures = VkPhysicalDeviceFeatures.callocStack(stack);
    vkGetPhysicalDeviceFeatures(device, deviceFeatures);

    if (!deviceFeatures.geometryShader()) {
      log.warn("* > !!! Device {} does not support geometry shaders", deviceProperties.deviceNameString());
      score = 1;
    }

    final var queueFamilies = getDeviceQueueFamilies(device, surface, stack);
    log.debug("* > Has graphics queue family: {}", queueFamilies.getGraphicsFamily() != null);
    log.debug("* > Has presentation queue family: {}", queueFamilies.getPresentationFamily() != null);

    if (!queueFamilies.isSuitable()) {
      log.warn("* > !!! Device {} does not have both a graphics and presentation queue family", deviceProperties.deviceNameString());
      score = 0;
    }

    if (!checkDeviceRequiredExtensions(device, stack)) {
      log.warn("* > !!! Device {} does not support all required extensions", deviceProperties.deviceNameString());
      score = 0;
    }

    log.debug("* > {} got score {}", deviceProperties.deviceNameString(), score);
    return new PhysicalDevice(score, device, deviceProperties, queueFamilies);
  }

  private static PhysicalDeviceQueueFamilies getDeviceQueueFamilies(VkPhysicalDevice physicalDevice, long surface, MemoryStack stack){
    final var queueFamilyPropertyCount = stack.callocInt(1);
    vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyPropertyCount, null);

    final var queueProps = VkQueueFamilyProperties.calloc(queueFamilyPropertyCount.get(0));
    vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyPropertyCount, queueProps);

    Integer graphicsSupportIndex = null;
    Integer presentationSupportIndex = null;

    for (int index = 0; index < queueFamilyPropertyCount.get(0); index++) {
      final var queueFamilyProperties = queueProps.get(index);

      if ((queueFamilyProperties.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
        if(graphicsSupportIndex == null) {
          graphicsSupportIndex = index;
        }

        final var supportsPresent = stack.callocInt(queueFamilyProperties.queueCount());
        for (int i = 0; i < queueFamilyProperties.queueCount(); i++) {
          supportsPresent.position(i);
          vkErrorCheck(vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, supportsPresent), "Failed to get physical device support");

          if (supportsPresent.get(i) == VK_TRUE && presentationSupportIndex == null) {
            presentationSupportIndex = index;
          }
        }
      }

      if(graphicsSupportIndex!=null && presentationSupportIndex!=null){
        break;
      }
    }

    return new PhysicalDeviceQueueFamilies(graphicsSupportIndex, presentationSupportIndex, queueProps);
  }

  private static boolean checkDeviceRequiredExtensions(VkPhysicalDevice device, MemoryStack stack) {
    log.debug("* Checking device extensions...");
    final var pExtensionCount = stack.callocInt(1);
    vkErrorCheck(vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, pExtensionCount, null), "Failed to get device extensions count");
    final var extensionProperties = VkExtensionProperties.callocStack(pExtensionCount.get(0), stack);
    vkErrorCheck(vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, pExtensionCount, extensionProperties), "Failed to get physical device extensions' properties");

    final var availableExtensions = new ArrayList<>(pExtensionCount.get(0));
    log.debug("* Available extensions:");
    for (int i = 0; i < pExtensionCount.get(0); i++) {
      availableExtensions.add(extensionProperties.get(i).extensionNameString());
      log.debug("* > {}", extensionProperties.get(i).extensionNameString());
    }

    log.debug("* Required extensions extensions:");
    var allExtensionsAvailable = true;

    for (final var extension : REQUIRED_DEVICE_EXTENSIONS) {
      final var available = availableExtensions.contains(extension);
      log.debug("* > {} - supported: {}", extension, available);
      if (!available) {
        allExtensionsAvailable = false;
      }
    }

    return allExtensionsAvailable;
  }
}
