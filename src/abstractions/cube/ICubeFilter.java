package abstractions.cube;

import abstractions.Orientation;

public interface ICubeFilter extends Cloneable {
    /**
     * Checks if the given cube matches this filter in its current orientation
     */
    default boolean match(ICube cube){
        return this.match(cube.getTriangles());
    }

    /**
     * Checks if the given cube matches this filter in the given orientation
     */
    default boolean match(ICube cube, Orientation o) {
        return this.match(cube.getTriangles(o));
    }

    /**
     * Checks if the given cube matches this filter
     */
    @Deprecated
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

    /**
     * Modifies this side of the filter
     */
    void setSide(byte side, byte triangle);

    Triangle getSide(ICube.Side side);

    /**
     * Returns the number of triangles on this thing.
     */
    int getNumTriangle();

    /**
     * Returns a replica of this filter; Should default to Objects.clone.
     */
    ICubeFilter cloneFilter();

    /**
     * Returns a unique id matching this filter.
     */
    int getUniqueId();
}
