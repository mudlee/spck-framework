package spck.core.renderer.backend.vulkan;

import com.conversantmedia.util.concurrent.ConcurrentQueue;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.GraphicsContext;
import spck.core.renderer.SubmitCommand;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK11.*;

public class VulkanContext extends GraphicsContext {
	private static final Logger log = LoggerFactory.getLogger(VulkanContext.class);
	private static final boolean VALIDATION_ENABLED = Boolean.parseBoolean(System.getProperty("vulkan.validation", "false"));
	private static final ByteBuffer[] VALIDATION_LAYERS = {memUTF8("VK_LAYER_LUNARG_standard_validation")};
	private static final boolean debug = System.getProperty("NDEBUG") == null;
	private static ByteBuffer[] layers = {
		memUTF8("VK_LAYER_LUNARG_standard_validation"),
	};

	@Override
	public void init() {
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);

		VkApplicationInfo appInfo = VkApplicationInfo.calloc();
		appInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
		appInfo.pApplicationName(memUTF8("SPCK DEMO")); // TODO make it to a parameter
		appInfo.applicationVersion(VK_MAKE_VERSION(1, 0, 0)); // TODO make it to a parameter
		appInfo.pEngineName(memUTF8("SPCK"));
		appInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0)); // TODO make it to a parameter
		appInfo.apiVersion(VK_API_VERSION_1_1);

		PointerBuffer requiredExtensions = glfwGetRequiredInstanceExtensions();
		if (requiredExtensions == null) {
			throw new AssertionError("Failed to find list of required Vulkan extensions");
		}
		PointerBuffer ppEnabledExtensionNames = memAllocPointer(requiredExtensions.remaining() + 1);
		ppEnabledExtensionNames.put(requiredExtensions);
		ByteBuffer VK_EXT_DEBUG_REPORT_EXTENSION = memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
		ppEnabledExtensionNames.put(VK_EXT_DEBUG_REPORT_EXTENSION);
		ppEnabledExtensionNames.flip();
		PointerBuffer ppEnabledLayerNames = memAllocPointer(layers.length);
		for (int i = 0; debug && i < layers.length; i++)
			ppEnabledLayerNames.put(layers[i]);
		ppEnabledLayerNames.flip();
		// TODO https://vulkan-tutorial.com/Drawing_a_triangle/Setup/Instance "Checking for extension support"

		VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc();
		createInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
		createInfo.ppEnabledExtensionNames(ppEnabledExtensionNames);
		createInfo.ppEnabledLayerNames(ppEnabledLayerNames);
		createInfo.pApplicationInfo(appInfo);

		PointerBuffer instance = memAllocPointer(1);
		int err = vkCreateInstance(createInfo, null, instance);
		long instanceId = instance.get(0);
		memFree(instance);
		if (err != VK_SUCCESS) {
			throw new AssertionError("Failed to create VkInstance: " + VulkanResult.translate(err));
		}
		VkInstance ret = new VkInstance(instanceId, createInfo);

		createInfo.free();
		memFree(ppEnabledLayerNames);
		memFree(VK_EXT_DEBUG_REPORT_EXTENSION);
		memFree(ppEnabledExtensionNames);
		memFree(appInfo.pApplicationName());
		memFree(appInfo.pEngineName());
		appInfo.free();
	}

	private static void checkValidationLayers() {
		log.debug("Checking validation layers...");
		IntBuffer validationLayerCount = memAllocInt(1);
		int result = vkEnumerateInstanceLayerProperties(validationLayerCount, null);
		// TODO
		//VkResultChecker.check(result, "Unabled to get validation layers' count.");
		log.debug("There are {} available validation layers:", validationLayerCount.get(0));

		VkLayerProperties.Buffer layerProperties = VkLayerProperties.calloc(validationLayerCount.get(0));
		result = vkEnumerateInstanceLayerProperties(validationLayerCount, layerProperties);
		// TODO
		//VkResultChecker.check(result, "Unabled to get validation layers.");
		for (int i = 0; i < validationLayerCount.get(0); i++) {
			VkLayerProperties properties = layerProperties.get(i);
			log.debug("\t{}, ver: {}, desc: {}", properties.layerNameString(), properties.specVersion(), properties.descriptionString());
		}
		layerProperties.free();
		memFree(validationLayerCount);
	}

	private static Map<String, ByteBuffer> constructExtensionBuffers() {
		log.debug("Constructing extension buffers...");

		Map<String, ByteBuffer> extensionBuffers = new HashMap<>();

		// debug extensions
		extensionBuffers.put(VK_EXT_DEBUG_REPORT_EXTENSION_NAME, memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME));
		extensionBuffers.put(VK_EXT_DEBUG_UTILS_EXTENSION_NAME, memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME));

		log.debug("- Added extension: {}", VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
		log.debug("- Added extension: {}", VK_EXT_DEBUG_UTILS_EXTENSION_NAME);

		return extensionBuffers;
	}

	/**
	 * Vulkan introduces an elegant system for this known as validation layers. Validation layers are optional
	 * components that hook into Vulkan function calls to apply additional operations.
	 *
	 * @see <a href="https://vulkan-tutorial.com/Drawing_a_triangle/Setup/Validation_layers">https://vulkan-tutorial.com/Drawing_a_triangle/Setup/Validation_layers</a>
	 */
	private static Optional<PointerBuffer> constructValidationLayers() {
		log.debug("Constructing validation layers: {}", VALIDATION_ENABLED);

		if (!VALIDATION_ENABLED) {
			log.debug("- vulkan.validation system property is not true");
			return Optional.empty();
		}

		PointerBuffer layers = memAllocPointer(VALIDATION_LAYERS.length);
		for (int i = 0; i < VALIDATION_LAYERS.length; i++) {
			log.debug("- Adding layer: {}", VALIDATION_LAYERS[i]);
			layers.put(VALIDATION_LAYERS[i]);
		}
		layers.flip();
		log.debug("- DONE");
		return Optional.of(layers);
	}

	@Override
	public void windowCreated(long windowId, int windowWidth, int windowHeight, boolean vSync, boolean debug) {

	}

	@Override
	public void setClearFlags(int mask) {

	}

	@Override
	public void setClearColor(Vector4f color) {

	}

	@Override
	public void clear() {

	}

	@Override
	public void swapBuffers(float frameTime, ConcurrentQueue<SubmitCommand> commandQueue) {
		while (!commandQueue.isEmpty()) {
			commandQueue.poll();
		}
	}

	@Override
	public void windowResized(int newWidth, int newHeight) {

	}

	@Override
	public void dispose() {
		MemoryAllocator.dispose();
	}
}
