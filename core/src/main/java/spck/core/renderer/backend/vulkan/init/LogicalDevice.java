package spck.core.renderer.backend.vulkan.init;

import org.lwjgl.vulkan.VkDevice;
import spck.core.renderer.Disposable;

import static org.lwjgl.vulkan.VK10.vkDestroyDevice;

public class LogicalDevice implements Disposable {
    private final VkDevice device;
    private final int presentationQueueFamilyIndex;

    public LogicalDevice(VkDevice device, int presentationQueueFamilyIndex) {
        this.device = device;
        this.presentationQueueFamilyIndex = presentationQueueFamilyIndex;
    }

    @Override
    public void dispose() {
        vkDestroyDevice(device, null);
    }

    public VkDevice getDevice() {
        return device;
    }

    public int getPresentationQueueFamilyIndex() {
        return presentationQueueFamilyIndex;
    }
}
