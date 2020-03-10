package spck.core.render;

import org.joml.Vector3f;

public class DefaultMaterial implements Material {
	private Vector3f diffuseColor = new Vector3f(0.5f, 0.5f, 0.5f);
	private Vector3f specularColor = new Vector3f(0.5f, 0.5f, 0.5f);
	private Vector3f ambientColor = new Vector3f(0.5f, 0.5f, 0.5f);

	public void setDiffuseColor(Vector3f diffuseColor) {
		this.diffuseColor = diffuseColor;
	}

	public void setSpecularColor(Vector3f specularColor) {
		this.specularColor = specularColor;
	}

	public void setAmbientColor(Vector3f ambientColor) {
		this.ambientColor = ambientColor;
	}

	@Override
	public Vector3f getDiffuseColor() {
		return diffuseColor;
	}

	@Override
	public Vector3f getSpecularColor() {
		return specularColor;
	}

	@Override
	public Vector3f getAmbientColor() {
		return ambientColor;
	}
}
