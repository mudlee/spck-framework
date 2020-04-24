package spck.core.renderer;

public class SubmitCommand {
    private final String name;
    private VertexArray vertexArray;
    private Shader shader;

    private SubmitCommand(String name){
        this.name = name;
    }

    public static SubmitCommand indexed(VertexArray vertexArray, Shader shader, String name) {
        SubmitCommand command = new SubmitCommand(name);
        command.vertexArray = vertexArray;
        command.shader = shader;
        return command;
    }

    public VertexArray getVertexArray() {
        return vertexArray;
    }

    public Shader getShader() {
        return shader;
    }

    public void dispose() {
        vertexArray.dispose();
        shader.dispose();
    }

    public String describe() {
        return name;
    }
}
