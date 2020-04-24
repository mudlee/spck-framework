package spck.core.renderer;

public class VertexLayoutAttribute {
    private final int position;
    private final int dataSize;
    private final int dataType;
    private final boolean normalized;

    public VertexLayoutAttribute(int position, int dataSize, int dataType, boolean normalized) {
        this.position = position;
        this.dataSize = dataSize;
        this.dataType = dataType;
        this.normalized = normalized;
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
}
