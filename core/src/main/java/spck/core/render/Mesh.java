package spck.core.render;

import org.joml.AABBf;
import spck.core.render.bgfx.IndexBuffer;
import spck.core.render.bgfx.VertexBuffer;
import spck.core.render.lifecycle.Disposable;

public class Mesh implements Disposable {
	private final VertexBuffer vertices;
	private final IndexBuffer indices;
	private final AABBf aabb;

	public Mesh(VertexBuffer vertices, IndexBuffer indices, AABBf aabb) {
		this.vertices = vertices;
		this.indices = indices;
		this.aabb = aabb;
	}

	@Override
	public void dispose() {
		vertices.dispose();
		indices.dispose();
	}

	public VertexBuffer getVertices() {
		return vertices;
	}

	public IndexBuffer getIndices() {
		return indices;
	}

	public AABBf getAabb() {
		return aabb;
	}
}
