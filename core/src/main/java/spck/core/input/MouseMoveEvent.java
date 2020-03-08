package spck.core.input;

import org.joml.Vector2d;
import spck.core.MoveDirection;

public class MouseMoveEvent {
    public final Vector2d position = new Vector2d();
    public final Vector2d relativePosition = new Vector2d();
    public final Vector2d offset = new Vector2d();
    public MoveDirection direction = MoveDirection.STILL;
}
