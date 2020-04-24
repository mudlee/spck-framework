package spck.core.renderer.backend.bgfx;

import spck.core.renderer.DataType;

import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_TYPE_FLOAT;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_TYPE_UINT8;

public class BGFXDataType extends DataType {
    public BGFXDataType() {
        super(
            BGFX_ATTRIB_TYPE_FLOAT,
            BGFX_ATTRIB_TYPE_UINT8
        );
    }
}
