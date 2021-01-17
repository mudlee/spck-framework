package spck.core.renderer.backend.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.core.io.ResourceLoader;
import spck.core.renderer.Shader;
import spck.core.renderer.backend.SPIRVCompiler;
import spck.core.renderer.backend.vulkan.init.Swapchain;
import spck.core.renderer.backend.vulkan.init.VkSwapchainCreator;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static spck.core.renderer.backend.vulkan.VkErrorChecker.vkErrorCheck;

public class VulkanShader extends Shader {
  private static final Logger log = LoggerFactory.getLogger(VulkanShader.class);

  public VulkanShader(String vertexShaderName, String fragmentShaderName) {
    try(final var stack = stackPush()) {
      log.debug("Creating shader program for with vertex shader '{}' and fragment shader '{}'", vertexShaderName, fragmentShaderName);

      final var vertexSPIRV = SPIRVCompiler.compile(
          ResourceLoader.loadToByteBuffer(String.format("/shaders/%s.glsl", vertexShaderName), stack),
          Shaderc.shaderc_vertex_shader,
          vertexShaderName
      );

      final var fragmentSPIRV = SPIRVCompiler.compile(
          ResourceLoader.loadToByteBuffer(String.format("/shaders/%s.glsl", fragmentShaderName), stack),
          Shaderc.shaderc_fragment_shader,
          fragmentShaderName
      );

      log.debug("* Creating vertex shader...");
      final var vertexModule = createShader(vertexSPIRV.getBytes(), stack);
      log.debug("* > Created");

      log.debug("* Creating fragment shader...");
      final var fragmentModule = createShader(fragmentSPIRV.getBytes(), stack);
      log.debug("* > Created");

      final var entryPoint = stack.UTF8("main");

      final var stages = VkPipelineShaderStageCreateInfo.callocStack(2, stack);
      stages
          .get(0)
          .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
          .module(vertexModule)
          .pName(entryPoint)
          .stage(VK_SHADER_STAGE_VERTEX_BIT);

      stages
          .get(1)
          .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
          .module(fragmentModule)
          .pName(entryPoint)
          .stage(VK_SHADER_STAGE_FRAGMENT_BIT);

      vertexSPIRV.dispose();
      fragmentSPIRV.dispose();
      vkDestroyShaderModule(VulkanContext.logicalDevice.getDevice(), vertexModule, null);
      vkDestroyShaderModule(VulkanContext.logicalDevice.getDevice(), fragmentModule, null);




      final var vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
      vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);

      final var inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
      inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
      inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
      inputAssembly.primitiveRestartEnable(false);

      final var viewport = VkViewport.callocStack(stack);
      viewport.x(0);
      viewport.y(0);
      viewport.width(VkSwapchainCreator.swapchain.getExtent().width());
      viewport.height(VkSwapchainCreator.swapchain.getExtent().height());
      viewport.minDepth(0);
      viewport.maxDepth(1);

      final var scissor = VkRect2D.callocStack(stack);
      scissor.offset(VkOffset2D.callocStack(stack).set(0,0));
      scissor.extent(VkSwapchainCreator.swapchain.getExtent());

      final var viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack);
      viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
      viewportState.viewportCount(1);
      // viewportState.pViewports = &viewport; // TODO
      // viewportState.pScissors = &scissor; // TODO
      viewportState.scissorCount(1);

      final var rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(stack);
      rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
      rasterizer.depthClampEnable(false);
      rasterizer.rasterizerDiscardEnable(false);
      rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
      rasterizer.lineWidth(1);
      rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
      rasterizer.frontFace(VK_FRONT_FACE_CLOCKWISE);
      rasterizer.depthBiasEnable(false);

      final var multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
      multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
      multisampling.sampleShadingEnable(false);
      multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);

      final var colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(stack);
      colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
      colorBlendAttachment.blendEnable(false);

      final var colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
      colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
      colorBlending.logicOpEnable(false);
      //colorBlending.pAttachments(????) // TODO

      //final var dynamicStats = VkPipelineDynamicStateCreateInfo.callocStack(stack);
      //dynamicStats.

      final var pPipelineLayout = stack.callocLong(1);
      final var pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
      pipelineLayoutCreateInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
      vkErrorCheck(vkCreatePipelineLayout(VulkanContext.logicalDevice.getDevice(), pipelineLayoutCreateInfo, null, pPipelineLayout), "Failed to create pipeline layout");
      // TODO: vkDestroyPipelineLayout(device, pipelineLayout, nullptr);
    }
  }

  private long createShader(byte[] code, MemoryStack stack) {
    final var pCode = memCalloc(code.length).put(code).flip();
    final var createInfo = VkShaderModuleCreateInfo.callocStack(stack)
        .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
        .pCode(pCode);

    final var pShader= stack.callocLong(1);
    vkErrorCheck(vkCreateShaderModule(VulkanContext.logicalDevice.getDevice(),createInfo, null, pShader), "Failed to create shader");

    // vkDestroyShaderModule TODO
    return pShader.get(0);
  }

  @Override
  public int getProgram() {
    return 0;
  }

  @Override
  public void bind() {

  }

  @Override
  public void unbind() {

  }

  @Override
  public void dispose() {

  }
}
