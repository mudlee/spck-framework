package spck.desktop;

import org.joml.Matrix4f;
import spck.core.app.Application;
import spck.core.eventbus.MessageBus;
import spck.core.graphics.Color;
import spck.core.renderer.*;
import spck.core.window.DesktopWindowPreferences;
import spck.core.eventbus.Event;
import spck.core.app.events.DisposeEvent;
import spck.core.app.events.InitializedEvent;
import spck.core.app.events.UpdateEvent;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class Example extends Application {
	private static final float[] cubeVertices = {
			-1.0f, 1.0f, 1.0f, 0xff000000,
			1.0f, 1.0f, 1.0f, 0xff0000ff,
			-1.0f, -1.0f, 1.0f, 0xff00ff00,
			1.0f, -1.0f, 1.0f, 0xff00ffff,
			-1.0f, 1.0f, -1.0f, 0xffff0000,
			1.0f, 1.0f, -1.0f, 0xffff00ff,
			-1.0f, -1.0f, -1.0f, 0xffffff00,
			1.0f, -1.0f, -1.0f, 0xffffffff
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

	private static final float[] triVert = {
		-0.5f,-0.5f,0.0f,0xff000000,
		0.5f,-0.5f,0.0f,0xff000000,
		0.0f,0.5f,0.0f,0xff000000
	};

	private static final int[] triInd = {
		0,1,2
	};

	private short program;

	private Matrix4f view = new Matrix4f();
	private FloatBuffer viewBuf;
	private Matrix4f proj = new Matrix4f();
	private FloatBuffer projBuf;
	private Matrix4f model = new Matrix4f();
	private FloatBuffer modelBuf;

	private SubmitCommand triangle;

	public Example() {
		super(DesktopWindowPreferences.Builder.create().build());
		MessageBus.global.subscribe(InitializedEvent.key, this::initialized);
		MessageBus.global.subscribe(UpdateEvent.key,this::update);
		MessageBus.global.subscribe(DisposeEvent.key,this::dispose);
	}

	public static void main(String[] args) {
		new Example().run();
	}

	public void initialized() {
		/*
		this.modelLoader = new ModelLoader(bgfxLayoutContext);
		cube = modelLoader.load("/assets/models/primitives/cube.obj");
		*/
		Shader shader = Shader.create("vs_cubes", "fs_cubes");

		VertexBufferLayout vertexBufferLayout = new VertexBufferLayout(
			new VertexLayoutAttribute(0, 3, Renderer.dataType.FLOAT,false),
			new VertexLayoutAttribute(4, 4, Renderer.dataType.UINT8,false)
		);

		VertexArray vertexArray = VertexArray.create();
		vertexArray.addVertexBuffer(VertexBuffer.create(triVert, vertexBufferLayout));
		vertexArray.setIndexBuffer(IndexBuffer.create(triInd));

		triangle = SubmitCommand.indexed(vertexArray,shader, "Triangle");

		window.input.onKeyPressed(GLFW_KEY_ESCAPE,event -> stop());
	}

	public void update(Event event) {
		Renderer.startScene();
		Renderer.setClearColor(Color.RED);
		Renderer.clear();
		Renderer.submit(triangle);
		Renderer.endScene();

		/*bgfx_dbg_text_printf(0, 1, 0x4f, "bgfx/examples/01-cubes");
		bgfx_dbg_text_printf(0, 2, 0x6f, "Description: Rendering simple static mesh.");
		bgfx_dbg_text_printf(0, 3, 0x0f, String.format("Frame: %7.3f[ms]", ((UpdateEvent)event).frameTime));
		//bgfx_dbg_text_printf(0, 4, 0x0f, String.format("RENDER %s", BGFXDemoUtil.getRendererType()));

		renderer.mainRenderer.lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, -35.0f), view);
		renderer.mainRenderer.perspective(60.0f, window.getWidth(), window.getHeight(), 0.1f, 100.0f, proj);

		bgfx_set_view_transform(0, view.get(viewBuf), proj.get(projBuf));

		long encoder = bgfx_encoder_begin(false);

		bgfx_encoder_set_transform(encoder,
				model.translation(
						0f,
						0f,
						0f
				)
						.rotateAffineXYZ(
								((UpdateEvent)event).deltaTime,
								((UpdateEvent)event).deltaTime,
								0.0f)
						.get(modelBuf));

		bgfx_encoder_set_vertex_buffer(encoder, 0, vertexBuffer.getBgfxBuffer(), 0, 8, BGFX_INVALID_HANDLE);
		bgfx_encoder_set_index_buffer(encoder, indexBuffer.getBgfxBuffer(), 0, 36);

		bgfx_encoder_set_state(encoder, BGFX_STATE_DEFAULT, 0);

		bgfx_encoder_submit(encoder, 0, program, 0, false);

		bgfx_encoder_end(encoder);*/
	}

	public void dispose() {
		triangle.dispose();
		/*MemoryUtil.memFree(viewBuf);
		MemoryUtil.memFree(projBuf);
		MemoryUtil.memFree(modelBuf);

		bgfx_destroy_program(program);

		cube.dispose();
		bgfxLayoutContext.dispose();*/
	}
}
