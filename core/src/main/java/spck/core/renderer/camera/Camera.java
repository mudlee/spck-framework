package spck.core.renderer.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class Camera {
    protected final Vector3f UP = new Vector3f(0, 1, 0);
    protected final Matrix4f view = new Matrix4f().identity();
    protected final Matrix4f projection = new Matrix4f().identity();
    protected final Vector3f position = new Vector3f().zero();
    public boolean viewMatrixChanged;

    protected void updateViewMatrix() {
        view.identity();
        //view.lookAt(position, frontVector, UP);
        viewMatrixChanged=true;
    }


}
