package abstractions.cube;

import abstractions.Orientation;

import java.util.ArrayList;

/**
 * Represents a single cube with 6 sides, each having one triangle (or lack thereof)
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
     * Returns the matching triangle of this for the given side.
     */
    default byte getMatchingTriangle(int side, boolean isVertical) {
        return (byte) this.getTriangle(Side.valueOf(side)).getMatching(isVertical).ordinal();
    }

    /**
     * Returns the matching triangle of this for the given side.
     */
    default byte getMatchingTriangle(int side) {
        return this.getMatchingTriangle(side, Side.isVertical[side]);
    }

    /**
     * Returns the unique identifier of this cube.
     */
    int getIdentifier();

    /**
     * Returns the number of triangles on this thing.
     */
    int getNumTriangles();

    /**
     * Returns a number unique for each cube no matter which rotation its in.
     */
    int getUniqueCubeId();

    /**
     * Returns a List of all possible {@link Orientation}s this cube matches the filter in.
     * The resulting list may be empty.
     */
    ArrayList<Orientation> match(ICubeFilter filter);

    /**
     * Returns the raw data of this cube. Do not modify the returned value.
     */
    byte[] getTriangles();

    /**
     * Returns the raw data of this cube. Do not modify the returned value.
     */
    byte[] getTriangles(Orientation o);

    byte[] getTriangles(int o);

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

        private static final boolean[] isVertical = new boolean[]{true, false, false, false, false, true};

        private static final Side[] opposite = {Down, Right, Back, Left, Front, Up};

        public Side getOpposite() {
            return opposite[this.ordinal()];
        }

        private static final byte[] oppositeBytes = {(byte) Down.ordinal(), (byte) Right.ordinal(), (byte) Back.ordinal(), (byte) Left.ordinal(), (byte) Front.ordinal(), (byte) Up.ordinal()};

        public static byte getOpposite(int side) {
            return oppositeBytes[side];
        }

        /**
         * Enum.values() clones the array to stop modification of the enum data (God, I sometimes hate this langauge).
         * We need performance, so we don't care about safety.
         */
        private static final Side[] values = values();

        /**
         * Faster brother of Enum.values(). DO NOT MODIFY THE RETURNED VALUE
         */
        public static Side[] getValues() {
            return values;
        }

        /**
         * Returns the side for the given ordinal.
         */
        public static Side valueOf(int ordinal) {
            assert ordinal >= 0 && ordinal < values.length;

            return values[ordinal];
        }

        public static Side getByDirection(int dx, int dy, int dz) {
            if(dx == 0 && dy == 0 && dz == 1) return Up;
            if(dx == 0 && dy == -1 && dz == 0) return Left;
            if(dx == 1 && dy == 0 && dz == 0) return Front;
            if(dx == 0 && dy == 1 && dz == 0) return Right;
            if(dx == -1 && dy == 0 && dz == 0) return Back;
            if(dx == 0 && dy == 0 && dz == -1) return Down;
            return null;
        }
    }
}
