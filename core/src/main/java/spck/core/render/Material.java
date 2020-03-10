package spck.core.render;

import org.joml.Vector3f;

public interface Material {
	Vector3f getDiffuseColor();

	Vector3f getSpecularColor();

	Vector3f getAmbientColor();
}
