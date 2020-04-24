package spck.core.window.input;

import org.joml.Vector2d;

public class MouseMoveEvent {
    public final Vector2d position = new Vector2d();
    public final Vector2d relativePosition = new Vector2d();
    public final Vector2d offset = new Vector2d();
    public MoveDirection direction = MoveDirection.STILL;
}
