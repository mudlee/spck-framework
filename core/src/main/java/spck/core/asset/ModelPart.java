package spck.core.asset;

import spck.core.render.Material;
import spck.core.render.Mesh;
import spck.core.render.lifecycle.Disposable;

public class ModelPart implements Disposable {
    private final Mesh mesh;
    private final Material material;

    public ModelPart(Mesh mesh, Material material) {
        this.mesh = mesh;
        this.material = material;
    }

    @Override
    public void dispose() {
        mesh.dispose();
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Material getMaterial() {
        return material;
    }
}
