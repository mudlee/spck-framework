package spck.core.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.bgfx.BGFXMemory;
import org.lwjgl.bgfx.BGFXReleaseFunctionCallback;
import org.lwjgl.bgfx.BGFXVertexLayout;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.lwjgl.bgfx.BGFX.*;
import static org.lwjgl.system.APIUtil.DEBUG_STREAM;
import static org.lwjgl.system.MemoryUtil.*;

public class MainRenderer {
	private static final BGFXReleaseFunctionCallback releaseMemoryCb = BGFXReleaseFunctionCallback.create((_ptr, _userData) -> nmemFree(_ptr));
	private static boolean zZeroToOne;
	private final int renderer;

	public MainRenderer(int renderer) {
		this.renderer = renderer;
		zZeroToOne = !bgfx_get_caps().homogeneousDepth();
	}

	public BGFXVertexLayout createVertexLayout(boolean withNormals, boolean withColor, int numUVs) {
		BGFXVertexLayout layout = BGFXVertexLayout.calloc();

		bgfx_vertex_layout_begin(layout, renderer);

		bgfx_vertex_layout_add(
				layout,
				BGFX_ATTRIB_POSITION,
				3,
				BGFX_ATTRIB_TYPE_FLOAT,
				false,
				false
		);

		if (withNormals) {
			bgfx_vertex_layout_add(
					layout,
					BGFX_ATTRIB_NORMAL,
					3,
					BGFX_ATTRIB_TYPE_FLOAT,
					false,
					false
			);
		}

		if (withColor) {
			bgfx_vertex_layout_add(
					layout,
					BGFX_ATTRIB_COLOR0,
					4,
					BGFX_ATTRIB_TYPE_UINT8,
					true,
					false
			);
		}

		if (numUVs > 0) {
			bgfx_vertex_layout_add(
					layout,
					BGFX_ATTRIB_TEXCOORD0,
					2,
					BGFX_ATTRIB_TYPE_FLOAT,
					false,
					false
			);
		}

		bgfx_vertex_layout_end(layout);
		return layout;
	}

	public short createVertexBuffer(ByteBuffer buffer, BGFXVertexLayout layout, Object[][] vertices) {
		for (Object[] vtx : vertices) {
			for (Object attr : vtx) {
				if (attr instanceof Float) {
					buffer.putFloat((float) attr);
				} else if (attr instanceof Integer) {
					buffer.putInt((int) attr);
				} else {
					throw new RuntimeException("Invalid parameter type");
				}
			}
		}

		if (buffer.remaining() != 0) {
			throw new RuntimeException("ByteBuffer size and number of arguments do not match");
		}

		buffer.flip();

		return createVertexBuffer(buffer, layout);
	}

	public short createVertexBuffer(ByteBuffer buffer, BGFXVertexLayout layout) {
		BGFXMemory vbhMem = bgfx_make_ref(buffer);
		return bgfx_create_vertex_buffer(vbhMem, layout, BGFX_BUFFER_NONE);
	}

	public short createIndexBuffer(ByteBuffer buffer, int[] indices) {
		for (int idx : indices) {
			buffer.putShort((short) idx);
		}

		if (buffer.remaining() != 0) {
			throw new RuntimeException("ByteBuffer size and number of arguments do not match");
		}

		buffer.flip();

		BGFXMemory ibhMem = bgfx_make_ref(buffer);
		return bgfx_create_index_buffer(ibhMem, BGFX_BUFFER_NONE);
	}

	public short loadShader(String name) {
		String resourcePath = "/shaders/";

		switch (renderer) {
			case BGFX_RENDERER_TYPE_DIRECT3D11:
			case BGFX_RENDERER_TYPE_DIRECT3D12:
				resourcePath += "dx11/";
				break;

			case BGFX_RENDERER_TYPE_DIRECT3D9:
				resourcePath += "dx9/";
				break;

			case BGFX_RENDERER_TYPE_OPENGL:
				resourcePath += "glsl/";
				break;

			case BGFX_RENDERER_TYPE_METAL:
				resourcePath += "metal/";
				break;
			default:
				throw new RuntimeException("No demo shaders supported for " + bgfx_get_renderer_name(renderer) + " renderer");
		}

		ByteBuffer shaderCode = loadResource(resourcePath, name + ".bin");

		return bgfx_create_shader(bgfx_make_ref_release(shaderCode, releaseMemoryCb, NULL));
	}

	private ByteBuffer loadResource(String resourcePath, String name) {
		try {
			URL url = MainRenderer.class.getResource(resourcePath + name);

			if (url == null) {
				throw new RuntimeException("Resource not found: " + resourcePath + name);
			}

			int resourceSize = url.openConnection().getContentLength();

			DEBUG_STREAM.println("bgfx: loading resource '" + url.getFile() + "' (" + resourceSize + " bytes)");

			ByteBuffer resource = memAlloc(resourceSize);

			try (BufferedInputStream bis = new BufferedInputStream(url.openStream())) {
				int b;
				do {
					b = bis.read();
					if (b != -1) {
						resource.put((byte) b);
					}
				} while (b != -1);
			}

			resource.flip();

			return resource;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void lookAt(Vector3f at, Vector3f eye, Matrix4f dest) {
		dest.setLookAtLH(eye.x, eye.y, eye.z, at.x, at.y, at.z, 0.0f, 1.0f, 0.0f);
	}

	public void perspective(float fov, int width, int height, float near, float far, Matrix4f dest) {
		float fovRadians = fov * (float) Math.PI / 180.0f;
		float aspect = width / (float) height;
		dest.setPerspectiveLH(fovRadians, aspect, near, far, zZeroToOne);
	}
}
