package spck.desktop;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import spck.core.Application;
import spck.core.DesktopWindowPreferences;
import spck.core.asset.Model;
import spck.core.asset.ModelLoader;
import spck.core.eventbus.Event;
import spck.core.render.bgfx.IndexBuffer;
import spck.core.render.bgfx.VertexBuffer;
import spck.core.render.bgfx.VertexLayoutContext;
import spck.core.render.lifecycle.DisposeEvent;
import spck.core.render.lifecycle.InitializedEvent;
import spck.core.render.lifecycle.UpdateEvent;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.bgfx.BGFX.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

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

	private short program;

	private Matrix4f view = new Matrix4f();
	private FloatBuffer viewBuf;
	private Matrix4f proj = new Matrix4f();
	private FloatBuffer projBuf;
	private Matrix4f model = new Matrix4f();
	private FloatBuffer modelBuf;
	private VertexLayoutContext bgfxLayoutContext;
	private ModelLoader modelLoader;
	private VertexBuffer vertexBuffer;
	private IndexBuffer indexBuffer;
	private Model cube;

	public Example() {
		super(DesktopWindowPreferences.Builder.create().build());
		appLifeCycle.subscribe(InitializedEvent.key, this::initialized);
		appLifeCycle.subscribe(UpdateEvent.key,this::update);
		appLifeCycle.subscribe(DisposeEvent.key,this::dispose);
	}

	public static void main(String[] args) {
		new Example().run();
	}

	public void initialized() {
		bgfxLayoutContext = new VertexLayoutContext();
		bgfxLayoutContext.create((context)->{
			context.add(
					BGFX_ATTRIB_POSITION,
					3,
					BGFX_ATTRIB_TYPE_FLOAT,
					false,
					false
			);
			context.add(
					BGFX_ATTRIB_COLOR0,
					4,
					BGFX_ATTRIB_TYPE_UINT8,
					true,
					false
			);
		});

		this.modelLoader = new ModelLoader(bgfxLayoutContext, appLifeCycle);
		cube = modelLoader.load("/assets/models/primitives/cube.obj");

		vertexBuffer = new VertexBuffer(cubeVertices,bgfxLayoutContext.getLayout());
		indexBuffer = new IndexBuffer(cubeIndices);

		short vs = mainRenderer.loadShader("vs_cubes");
		short fs = mainRenderer.loadShader("fs_cubes");

		program = bgfx_create_program(vs, fs, true);

		viewBuf = MemoryUtil.memAllocFloat(16);
		projBuf = MemoryUtil.memAllocFloat(16);
		modelBuf = MemoryUtil.memAllocFloat(16);

		input.onKeyPressed(GLFW_KEY_ESCAPE,event -> glfwSetWindowShouldClose(window.getId(), true));
	}

	public void update(Event event) {
		bgfx_dbg_text_printf(0, 1, 0x4f, "bgfx/examples/01-cubes");
		bgfx_dbg_text_printf(0, 2, 0x6f, "Description: Rendering simple static mesh.");
		bgfx_dbg_text_printf(0, 3, 0x0f, String.format("Frame: %7.3f[ms]", ((UpdateEvent)event).frameTime));
		//bgfx_dbg_text_printf(0, 4, 0x0f, String.format("RENDER %s", BGFXDemoUtil.getRendererType()));

		mainRenderer.lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, -35.0f), view);
		mainRenderer.perspective(60.0f, window.getWidth(), window.getHeight(), 0.1f, 100.0f, proj);

		bgfx_set_view_transform(0, view.get(viewBuf), proj.get(projBuf));

		long encoder = bgfx_encoder_begin(false);

		bgfx_encoder_set_transform(encoder,
				model.translation(
						0f,
						0f,
						0f
				)
						.rotateAffineXYZ(
								((UpdateEvent)event).time,
								((UpdateEvent)event).time,
								0.0f)
						.get(modelBuf));

		bgfx_encoder_set_vertex_buffer(encoder, 0, vertexBuffer.getBgfxBuffer(), 0, 8, BGFX_INVALID_HANDLE);
		bgfx_encoder_set_index_buffer(encoder, indexBuffer.getBgfxBuffer(), 0, 36);

		bgfx_encoder_set_state(encoder, BGFX_STATE_DEFAULT, 0);

		bgfx_encoder_submit(encoder, 0, program, 0, false);

		bgfx_encoder_end(encoder);
	}

	public void dispose() {
		MemoryUtil.memFree(viewBuf);
		MemoryUtil.memFree(projBuf);
		MemoryUtil.memFree(modelBuf);

		bgfx_destroy_program(program);

		cube.dispose();
		vertexBuffer.dispose();
		indexBuffer.dispose();
		bgfxLayoutContext.dispose();
	}
}
