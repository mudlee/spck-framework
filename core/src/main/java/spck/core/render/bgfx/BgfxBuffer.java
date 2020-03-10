package spck.core.render.bgfx;

import spck.core.render.lifecycle.Disposable;

import java.nio.ByteBuffer;


abstract public class BgfxBuffer implements Disposable {
	protected ByteBuffer buffer;
	protected short bgfxBuffer;
}
