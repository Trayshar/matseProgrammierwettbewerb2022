package abstractions.cube;

import abstractions.Orientation;

public interface ICubeFilter extends Cloneable {
    /**
     * Checks if the given cube matches this filter in its current orientation
     */
    default boolean match(ICube cube){
        return this.match(
                cube.getTriangle(ICube.Side.Up),
                cube.getTriangle(ICube.Side.Left),
                cube.getTriangle(ICube.Side.Front),
                cube.getTriangle(ICube.Side.Right),
                cube.getTriangle(ICube.Side.Back),
                cube.getTriangle(ICube.Side.Down)
        );
    }

    /**
     * Checks if the given cube matches this filter in the given orientation
     */
    default boolean match(ICube cube, Orientation o) {
        return this.match(
                cube.getTriangle(ICube.Side.Up, o),
                cube.getTriangle(ICube.Side.Left, o),
                cube.getTriangle(ICube.Side.Front, o),
                cube.getTriangle(ICube.Side.Right, o),
                cube.getTriangle(ICube.Side.Back, o),
                cube.getTriangle(ICube.Side.Down, o)
        );
    }

    /**
     * Checks if the given cube matches this filter
     */
    default boolean match(Triangle... cube){
        return this.match(
                (byte) cube[0].ordinal(),
                (byte) cube[1].ordinal(),
                (byte) cube[2].ordinal(),
                (byte) cube[3].ordinal(),
                (byte) cube[4].ordinal(),
                (byte) cube[5].ordinal()
        );
    }

    /**
     * Checks if the given cube matches this filter
     */
    boolean match(byte... triangles);

    /**
     * Modifies this side of the filter
     */
    void setSide(ICube.Side side, Triangle triangle);

    Triangle getSide(ICube.Side side);

    /**
     * Returns the number of triangles on this thing.
     */
    int getNumTriangle();

    /**
     * Returns a replica of this filter; Should default to Objects.clone.
     */
    ICubeFilter cloneFilter();
}
