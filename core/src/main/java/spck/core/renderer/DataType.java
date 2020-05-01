package spck.core.renderer;

public class DataType {
    public final int DATA_FLOAT;
    public final int COLOR_BUFFER;
    public final int DEPTH_BUFFER;
    public final int STENCIL_BUFFER;

    protected DataType(
        int dataFloat,
        int colorBuffer,
        int depthBuffer,
        int stencilBuffer
    ) {
        DATA_FLOAT = dataFloat;
        COLOR_BUFFER = colorBuffer;
        DEPTH_BUFFER = depthBuffer;
        STENCIL_BUFFER = stencilBuffer;
    }
}
