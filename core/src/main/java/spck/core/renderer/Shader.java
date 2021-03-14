package spck.core.renderer;

import spck.core.renderer.backend.opengl.OpenGLShader;

public abstract class Shader extends GraphicsData{
    public static Shader create(String vertexShaderName, String fragmentShaderName){
        switch (backend) {
	        case OPENGL:
		        return new OpenGLShader(vertexShaderName, fragmentShaderName);
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
