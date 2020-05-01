package spck.core.renderer.backend.opengl;

import static org.lwjgl.opengl.GL41.*;
import spck.core.renderer.DataType;

public class OpenGLDataType extends DataType {
    public OpenGLDataType() {
        super(
            GL_FLOAT,
            GL_COLOR_BUFFER_BIT,
            GL_DEPTH_BUFFER_BIT,
            GL_STENCIL_BUFFER_BIT
        );
    }
}
