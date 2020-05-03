package spck.core.renderer;

import spck.core.renderer.backend.RendererApi;
import spck.core.renderer.backend.opengl.OpenGLShader;
import spck.core.renderer.backend.vulkan.VulkanShader;

public abstract class Shader {
    public static Shader create(String vertexShaderName, String fragmentShaderName){
        switch (RendererApi.backend) {
	        case OPENGL:
		        return new OpenGLShader(vertexShaderName, fragmentShaderName);
	        case VULKAN:
		        return new VulkanShader(vertexShaderName, fragmentShaderName);
	        default:
		        throw new UnsupportedOperationException();
        }
    }

    public abstract int getProgram();

    public abstract void bind();

    public abstract void unbind();

    //public abstract void uploadUniform(vector, String name);

    public abstract void dispose();
}
