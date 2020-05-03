package spck.desktop;

import org.joml.Vector4f;
import spck.core.app.Application;
import spck.core.app.events.DisposeEvent;
import spck.core.app.events.InitializedEvent;
import spck.core.app.events.UpdateEvent;
import spck.core.eventbus.Event;
import spck.core.eventbus.MessageBus;
import spck.core.renderer.*;
import spck.core.renderer.camera.Camera;
import spck.core.renderer.camera.OrthoCamera;
import spck.core.window.DesktopWindowPreferences;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class Example extends Application {
	private static final float[] cubeVertices = {
			-1.0f, 1.0f, 1.0f, 1f, 0f, 0f, 1f,
			1.0f, 1.0f, 1.0f, 1f, 0f, 0f, 1f,
			-1.0f, -1.0f, 1.0f, 1f, 0f, 0f, 1f,
			1.0f, -1.0f, 1.0f, 1f, 0f, 0f, 1f,
			-1.0f, 1.0f, -1.0f, 1f, 0f, 0f, 1f,
			1.0f, 1.0f, -1.0f, 1f, 0f, 0f, 1f,
			-1.0f, -1.0f, -1.0f, 1f, 0f, 0f, 1f,
			1.0f, -1.0f, -1.0f, 1f, 0f, 0f, 1f
	};

	private static final int[] cubeIndices = {
			0, 1, 2, // 0
			1, 3, 2,
			4, 6, 5, // 2
			5, 6, 7,
			0, 2, 4, // 4
			4, 2, 6,
			1, 5, 3, // 6
			5, 7, 3,
			0, 4, 1, // 8
			4, 5, 1,
			2, 3, 6, // 10
			6, 3, 7
	};

	private static final float[] triVertColored = {
			-0.5f, -0.5f, 0.0f, 1f, 0f, 0f, 1f,
			0.5f, -0.5f, 0.0f, 0f, 1f, 0f, 1f,
			0.0f, 0.5f, 0.0f, 0f, 0f, 1f, 1f,
	};

	private static final int[] triInd = {
			0, 1, 2
	};

	private static final float[] squareColored = {
			-0.5f, -0.5f, 0.0f, 0.2f, 0.5f, 0.5f, 1f,
			0.5f, -0.5f, 0.0f, 0.2f, 0.5f, 0.5f, 1f,
			0.5f, 0.5f, 0.0f, 0.2f, 0.5f, 0.5f, 1f,
			-0.5f, 0.5f, 0.0f, 0.2f, 0.5f, 0.5f, 1f,
	};

	private static final int[] squareInd = {
			0, 1, 2, 2, 3, 0
	};

	private SubmitCommand square;
	private SubmitCommand triangle;
	private Camera camera = new OrthoCamera();

	public Example() {
		super(DesktopWindowPreferences.Builder.create().withRendererBackend(RendererBackend.OPENGL).build(), true);
		MessageBus.global.subscribe(InitializedEvent.key, this::initialized);
		MessageBus.global.subscribe(UpdateEvent.key, this::update);
		MessageBus.global.subscribe(DisposeEvent.key, this::dispose);
	}

	public static void main(String[] args) {
		new Example().run();
	}

	public void initialized() {
		Shader shader = Shader.create("simple.vert", "simple.frag");

		int stride = 7 * Float.BYTES;
		VertexBufferLayout layout = new VertexBufferLayout(
				new VertexLayoutAttribute(0, 3, Renderer.dataType.DATA_FLOAT, false, stride, 0),
				new VertexLayoutAttribute(1, 4, Renderer.dataType.DATA_FLOAT, false, stride, 3 * Float.BYTES)
		);

		VertexArray va1 = VertexArray.create();
		va1.addVertexBuffer(VertexBuffer.create(squareColored, layout));
		va1.setIndexBuffer(IndexBuffer.create(squareInd));

		VertexArray va2 = VertexArray.create();
		va2.addVertexBuffer(VertexBuffer.create(triVertColored, layout));
		va2.setIndexBuffer(IndexBuffer.create(triInd));

		square = SubmitCommand.indexed(va1, shader, "Square");
		triangle = SubmitCommand.indexed(va2, shader, "Triangle");

		window.input.onKeyPressed(GLFW_KEY_ESCAPE, event -> stop());

		Renderer.setClearColor(new Vector4f(0.2f, 0.2f, 0.2f, 1f));
		Renderer.setClearFlags(Renderer.dataType.COLOR_BUFFER | Renderer.dataType.DEPTH_BUFFER | Renderer.dataType.STENCIL_BUFFER);
	}

	public void update(Event event) {
		Renderer.startScene(camera);
		Renderer.clear();
		Renderer.submit(square);
		Renderer.submit(triangle);
		Renderer.endScene();
	}

	public void dispose() {
		triangle.dispose();
	}
}
