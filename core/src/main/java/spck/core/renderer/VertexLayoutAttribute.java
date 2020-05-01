package spck.core.renderer;

public class VertexLayoutAttribute {
    private final int position;
    private final int dataSize;
    private final int dataType;
    private final int stride;
    private final int offset;
    private final boolean normalized;

    public VertexLayoutAttribute(int position, int dataSize, int dataType, boolean normalized, int stride, int offset) {
        this.position = position;
        this.dataSize = dataSize;
        this.dataType = dataType;
        this.normalized = normalized;
        this.stride = stride;
        this.offset = offset;
    }

    public int getPosition() {
        return position;
    }

    public int getDataSize() {
        return dataSize;
    }

    public int getDataType() {
        return dataType;
    }

    public boolean isNormalized() {
        return normalized;
    }

    public int getStride() {
        return stride;
    }

    public int getOffset() {
        return offset;
    }
}
