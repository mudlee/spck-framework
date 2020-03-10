package spck.core.asset;

import spck.core.render.lifecycle.Disposable;

import java.util.List;

public class Model implements Disposable {
	private final List<ModelPart> parts;

	public void dispose() {
		parts.forEach(ModelPart::dispose);
	}

	public Model(List<ModelPart> parts) {
		this.parts = parts;
	}

	public List<ModelPart> getParts() {
		return parts;
	}
}
