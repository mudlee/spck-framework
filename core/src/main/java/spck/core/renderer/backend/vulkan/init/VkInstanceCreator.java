package spck.core.renderer.backend.vulkan.init;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.backend.vulkan.MemoryAllocator;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;
import static org.lwjgl.vulkan.VK11.VK_API_VERSION_1_1;
import static spck.core.renderer.backend.vulkan.VkErrorChecker.vkErrorCheck;

// TODO
// set up validation layers this way: https://vulkan-tutorial.com/Drawing_a_triangle/Setup/Validation_layers
public class VkInstanceCreator {
  private static final Logger log = LoggerFactory.getLogger(VkInstanceCreator.class);
  private static final ByteBuffer[] additionalExtensions = {
    MemoryAllocator.memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME),
  };
  private static final ByteBuffer[] additionalLayers = {
    MemoryAllocator.memUTF8("VK_LAYER_LUNARG_standard_validation"),
  };

  public static VkInstance create(boolean debug){
    try(final var stack = stackPush()) {
      log.debug("Creating VkInstance...");
      final var appInfo = VkApplicationInfo.callocStack(stack);
      appInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
      appInfo.pApplicationName(MemoryAllocator.memUTF8("")); // TODO make it to a parameter
      appInfo.applicationVersion(VK_MAKE_VERSION(0, 0, 1)); // TODO make it to a parameter
      appInfo.pEngineName(MemoryAllocator.memUTF8("SPCK"));
      appInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0)); // TODO make it to a parameter
      appInfo.apiVersion(Platform.get().equals(Platform.MACOSX) ? VK_API_VERSION_1_0 : VK_API_VERSION_1_1);

      log.debug("* Specifying extensions...");
      reportAvailableExtensions(stack);
      final var extensions = getRequiredExtensions(stack, additionalExtensions);

      log.debug("* Specifying layers...");
      final var availableLayers = reportAvailableLayers(stack);
      final var layers = getAdditionalLayers(debug, availableLayers, stack);

      log.debug("* Creating create info...");
      final var createInfo = VkInstanceCreateInfo.callocStack(stack);
      createInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
      createInfo.pApplicationInfo(appInfo);
      createInfo.ppEnabledExtensionNames(extensions);
      createInfo.ppEnabledLayerNames(layers);

      log.debug("* Creating instance...");
      final var instance = stack.callocPointer(1);
      vkErrorCheck(vkCreateInstance(createInfo, null, instance), "Failed to create VkInstance");

      final var instanceId = instance.get(0);
      final var vkInstance = new VkInstance(instanceId, createInfo);

      log.debug("* VkInstance created, cleaning up...");

      return vkInstance;
    }
  }

  private static PointerBuffer getRequiredExtensions(MemoryStack stack, ByteBuffer... additionalExtension) {
    final var requiredExtensions = glfwGetRequiredInstanceExtensions();
    if (requiredExtensions == null) {
      throw new RuntimeException("Failed to find list of required Vulkan extensions");
    }

    final var extensions = stack.callocPointer(requiredExtensions.remaining() + additionalExtension.length);
    extensions.put(requiredExtensions);

    for (ByteBuffer byteBuffer : additionalExtension) {
      extensions.put(byteBuffer);
    }

    extensions.flip();
    return extensions;
  }

  private static PointerBuffer getAdditionalLayers(boolean debug, List<String> availableLayers, MemoryStack stack) {
    final var layers = stack.callocPointer(VkInstanceCreator.additionalLayers.length);
    
    for (int i = 0; debug && i < VkInstanceCreator.additionalLayers.length; i++) {
      final var name = StandardCharsets.ISO_8859_1.decode(VkInstanceCreator.additionalLayers[i].duplicate()).toString();

      if(!availableLayers.contains(name.replace("\0", ""))) {
        log.warn("* !!! Layer {} is not supported", name);
        continue;
      }

      log.debug("* Layer {} added", name);
      layers.put(VkInstanceCreator.additionalLayers[i]);
    }
    
    layers.flip();
    return layers;
  }

  private static void reportAvailableExtensions(MemoryStack stack) {
    final var extensionCount = stack.callocInt(1);
    vkErrorCheck(vkEnumerateInstanceExtensionProperties((ByteBuffer) null, extensionCount, null), "Unabled to query Vulkan extension count");

    log.debug("* There are {} available extensions:", extensionCount.get(0));
    final var extensionProperties = VkExtensionProperties.callocStack(extensionCount.get(0), stack);
    vkErrorCheck(vkEnumerateInstanceExtensionProperties((ByteBuffer) null, extensionCount, extensionProperties), "Unabled to query Vulkan extension properties");

    for (int i = 0; i < extensionCount.get(0); i++) {
      final var properties = extensionProperties.get(i);
      log.debug("* > {}, ver: {}", properties.extensionNameString(), properties.specVersion());
    }
  }

  private static List<String> reportAvailableLayers(MemoryStack stack) {
    final var validationLayerCount = stack.callocInt(1);
    vkErrorCheck(vkEnumerateInstanceLayerProperties(validationLayerCount, null), "Unabled to get validation layers' count");
    log.debug("* There are {} available validation layers:", validationLayerCount.get(0));

    final var layerProperties = VkLayerProperties.callocStack(validationLayerCount.get(0), stack);
    vkErrorCheck(vkEnumerateInstanceLayerProperties(validationLayerCount, layerProperties), "Unabled to get validation layers");

    final var availableLayers = new ArrayList<String>();

    for (int i = 0; i < validationLayerCount.get(0); i++) {
      final var properties = layerProperties.get(i);
      availableLayers.add(properties.layerNameString());
      log.debug("* > {}, ver: {}, desc: {}", properties.layerNameString(), properties.specVersion(), properties.descriptionString());
    }

    return availableLayers;
  }
}
