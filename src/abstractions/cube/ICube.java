package abstractions.cube;

import abstractions.Axis;

/**
 * Represents a single cube with 6 sides, each having one triangle (or not)
 */
public interface ICube extends Cloneable {

//    default void rotate(Axis axis) {
//        this.rotate(axis, 1);
//    }
//
//    void rotate(Axis axis, int amount);

    Triangle getTriangle(Side side);

    int getIdentifier();

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

    enum Side {
        Up(0, 1, 0), Left(-1, 0, 0), Front(0, 0, 1), Right(1, 0, 0), Back(0, 0, -1), Down(0, -1, 0);

        public final int x, y, z;

        Side(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
