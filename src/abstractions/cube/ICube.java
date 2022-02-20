package abstractions.cube;

import abstractions.Orientation;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Represents a single cube with 6 sides, each having one triangle (or not)
 */
public interface ICube extends Cloneable {

    /**
     * Returns a clone of this cube. Should delegate to Object.clone().
     */
    ICube cloneCube();

    /**
     * Sets the orientation of this cube
     */
    void setOrientation(Orientation orientation);

    /**
     * Returns the triangle at the given side in its current orientation
     */
    Triangle getTriangle(Side side);

    /**
     * Returns the triangle at the given side with the given orientation. Does not modify the orientation of this cube.
     */
    Triangle getTriangle(Side side, Orientation orientation);

    /**
     * Returns the unique identifier of this cube.
     */
    int getIdentifier();

    /**
     * Returns the number of triangles on this thing.
     */
    int getNumTriangles();

    /**
     * Returns a {@link Stream} of all possible {@link Orientation}s this cube matches the filter in.
     * The resulting stream may be empty.
     */
    ArrayList<Orientation> match(ICubeFilter filter);

    byte[] getTriangles();

    byte[] getTriangles(Orientation o);

    /**
     * Serializes the cube with its current orientation.
     */
    default String serialize() {
        StringBuilder b = new StringBuilder("Teil ");
        b.append(this.getIdentifier());
        b.append(": ");
        for (int i = 0; i < 6; i++) {
            b.append(getTriangle(Side.values()[i]).serialize());
            b.append(' ');
        }
        return b.toString().trim();
    }

    /**
     * Side of a cube. Rather self-explanatory.
     */
    enum Side {
        Up(0, 0, 1), Left(0, -1, 0), Front(1, 0, 0), Right(0, 1, 0), Back(-1, 0, 0), Down(0, 0, -1);

        public final int x, y, z;

        Side(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static final Side[] opposite = {Down, Right, Back, Left, Front, Up};

        public Side getOpposite() {
            return opposite[this.ordinal()];
        }
    }
}
