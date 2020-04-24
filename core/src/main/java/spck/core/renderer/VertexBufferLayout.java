package spck.core.renderer;

public class VertexBufferLayout {
    private final VertexLayoutAttribute[] attributes;

    public VertexBufferLayout(VertexLayoutAttribute... attributes) {
        this.attributes = attributes;
    }

    public VertexLayoutAttribute[] getAttributes(){
        return attributes;
    };
}
