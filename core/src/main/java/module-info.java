module spck.core {
	requires com.conversantmedia.disruptor;
	requires org.lwjgl;
	requires org.lwjgl.opengl;
	requires transitive org.lwjgl.glfw;
	requires org.lwjgl.assimp;
	requires transitive org.joml;
	requires transitive org.slf4j;

	exports spck.core.app;
	exports spck.core.app.events;
	exports spck.core.eventbus;
	exports spck.core.graphics;
	exports spck.core.renderer;
	exports spck.core.renderer.camera;
	exports spck.core.window;
	exports spck.core.window.input;
}