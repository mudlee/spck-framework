package spck.core.renderer.backend.vulkan;

import com.conversantmedia.util.concurrent.ConcurrentQueue;
import org.joml.Vector4f;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.renderer.GraphicsContext;
import spck.core.renderer.SubmitCommand;
import spck.core.renderer.backend.vulkan.init.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK11.*;

public class VulkanContext extends GraphicsContext {
	private static final Logger log = LoggerFactory.getLogger(VulkanContext.class);
	private final boolean debug;

	private VkInstance vkInstance;
	private long surfaceId;
	private Swapchain swapchain;
	private PhysicalDevice physicalDevice;
	public static LogicalDevice logicalDevice; // TODO

	public VulkanContext(boolean debug) {
		this.debug = debug;
	}

	@Override
	public void init(long windowId) {
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
	}

	@Override
	public void windowCreated(long windowId, int windowWidth, int windowHeight, boolean vSync) {
		vkInstance = VkInstanceCreator.create(debug);
		surfaceId = VkSurfaceCreator.create(vkInstance, windowId);
		physicalDevice = VkPhysicalDeviceSelector.select(vkInstance, surfaceId);
		logicalDevice = VkLogicalDeviceCreator.create(physicalDevice);
		final var x = VkQueueCreator.createPresentationQueue(logicalDevice);

		swapchain = VkSwapchainCreator.create(physicalDevice, logicalDevice, surfaceId, windowId);

		VkImageViewCreator.create(logicalDevice, swapchain);

		createRenderPass();
	}

	private void createRenderPass() {
		final var attachmentDesc = VkAttachmentDescription.calloc()
			.format(swapchain.getFormat().format())
			.samples(VK_SAMPLE_COUNT_1_BIT)
			.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
			.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
			.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
			.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
			.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
			.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

		final var attachmentRef = VkAttachmentReference.calloc()
			.attachment(0)
			.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

		/*final var subpass = VkSubpassDescription.calloc(1)
			.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
			.colorAttachmentCount(1)
			.pColorAttachments(attachmentRef);*/
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
			final var command = commandQueue.poll();
		}
	}

	@Override
	public void windowResized(int newWidth, int newHeight) {

	}

	@Override
	public void dispose() {
		log.debug("Disposing...");

		vkDestroySurfaceKHR(vkInstance, surfaceId, null);
		swapchain.dispose();
		physicalDevice.dispose();
		logicalDevice.dispose();
		vkDestroyInstance(vkInstance, null);
		MemoryAllocator.dispose();
	}
}
