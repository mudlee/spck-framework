package spck.core.renderer.backend.vulkan.init;

import org.lwjgl.vulkan.VkQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;

public class VkQueueCreator {
    private static final Logger log = LoggerFactory.getLogger(VkQueueCreator.class);

    public static VkQueue createPresentationQueue(LogicalDevice logicalDevice) {
        try(final var stack = stackPush()) {
            log.debug("Creating presentation queue, index: {}", logicalDevice.getPresentationQueueFamilyIndex());
            final var pQueue = stack.callocPointer(1);
            vkGetDeviceQueue(logicalDevice.getDevice(), logicalDevice.getPresentationQueueFamilyIndex(), 0, pQueue);
            final var vkQueue = new VkQueue(pQueue.get(0), logicalDevice.getDevice());

            log.debug("* Queue created");
            return vkQueue;
        }
    }
}
