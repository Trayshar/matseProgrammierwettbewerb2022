package abstractions.cube;

/**
 * Represents a single cube with 6 sides, each having one triangle (or not)
 */
public interface ICube {

    default void rotate(Axis axis) {
        this.rotate(axis, 1);
    }

    void rotate(Axis axis, int amount);

    String serialize();

    enum Side{
        None, BottomLeft, UpperLeft, UpperRight, BottomRight
    }

    enum Axis{
        X, Y, Z
    }
}
