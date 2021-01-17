package spck.core.renderer.backend.vulkan;

import org.lwjgl.vulkan.VkQueueFamilyProperties;
import spck.core.renderer.Disposable;

public class PhysicalDeviceQueueFamilies implements Disposable {
  private final Integer graphicsFamily;
  private final Integer presentationFamily;
  private final VkQueueFamilyProperties.Buffer queueProps;

  public PhysicalDeviceQueueFamilies(Integer graphicsFamily, Integer presentationFamily, VkQueueFamilyProperties.Buffer queueProps) {
    this.graphicsFamily = graphicsFamily;
    this.presentationFamily = presentationFamily;
    this.queueProps = queueProps;
  }

  @Override
  public void dispose() {
    queueProps.free();
  }

  public boolean isSuitable(){
    return graphicsFamily!=null && presentationFamily!=null;
  }

  public Integer getGraphicsFamily() {
    return graphicsFamily;
  }

  public Integer getPresentationFamily() {
    return presentationFamily;
  }

  public boolean areQueueFamiliesDifferent() {
    return !graphicsFamily.equals(presentationFamily);
  }
}
