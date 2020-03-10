module spck.core {
	requires transitive org.lwjgl;
	requires transitive org.lwjgl.bgfx;
	requires transitive org.lwjgl.glfw;
	requires transitive org.lwjgl.assimp;
	requires transitive org.joml;
	requires transitive org.slf4j;

	exports spck.core;
	exports spck.core.asset;
	exports spck.core.eventbus;
	exports spck.core.input;
	exports spck.core.render;
	exports spck.core.render.bgfx;
	exports spck.core.render.lifecycle;
	exports spck.core.util;
}