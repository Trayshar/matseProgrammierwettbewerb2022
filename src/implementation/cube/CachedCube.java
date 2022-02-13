package implementation.cube;

import abstractions.Axis;
import abstractions.cube.ICube;
import abstractions.cube.Triangle;

/**
 * Cube implementation that caches all possible rotations.
 */
public class CachedCube implements ICube {
    private final byte[][] data = new byte[24][6];
    private int orientation = 0;
    private final int identifier;

    public CachedCube(int identifier, Triangle up, Triangle left, Triangle front, Triangle right, Triangle back, Triangle down) {
        this.identifier = identifier;
    }

    @Override
    public void rotate(Axis axis, int amount) {
        amount = amount % 4;

        data[0][0] = 1;
        orientation = (orientation + amount)% 24;
    }

    @Override
    public Triangle getTriangle(Side side) {
        return Triangle.valueOf( data[orientation][side.ordinal()] );
    }

    @Override
    public int getIdentifier() {
        return identifier;
    }
}
