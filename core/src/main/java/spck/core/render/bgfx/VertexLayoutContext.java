package spck.core.render.bgfx;

import org.lwjgl.bgfx.BGFXMemory;
import org.lwjgl.bgfx.BGFXVertexLayout;
import spck.core.render.lifecycle.Disposable;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static org.lwjgl.bgfx.BGFX.*;

public class VertexLayoutContext implements Disposable {
	private static boolean setup;
	private static int renderer;
	private final BGFXVertexLayout layout;

	public static void setup(int renderer) {
		VertexLayoutContext.renderer = renderer;
		VertexLayoutContext.setup = true;
	}

	public VertexLayoutContext() {
		if(!setup) {
			throw new RuntimeException("BgfxLayout has not yet been initialized");
		}
		this.layout = BGFXVertexLayout.calloc();
	}

	public void create(Consumer<VertexLayoutContext> consumer) {
		bgfx_vertex_layout_begin(layout, renderer);
		consumer.accept(this);
		bgfx_vertex_layout_end(layout);
	}

	public void add(int type, int dataSize, int dataType, boolean normalized, boolean asInt){
		bgfx_vertex_layout_add(
				layout,
				type,
				dataSize,
				dataType,
				normalized,
				asInt
		);
	}

	public BGFXVertexLayout getLayout() {
		return layout;
	}

	@Override
	public void dispose() {
		layout.free();
	}
}
