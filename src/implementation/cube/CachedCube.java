package implementation.cube;

import abstractions.Orientation;
import abstractions.cube.ICube;
import abstractions.cube.Triangle;

import java.util.Arrays;

/**
 * Cube implementation that caches all possible rotations.
 */
public class CachedCube implements ICube {
    private final byte[][] data = new byte[24][6];
    private Orientation orientation = Orientation.Alpha;
    /** The unique number of this cube */
    public final int identifier;
    /** The number of triangles this cube has */
    public final int triangles;

    /**
     * @param identifier The unique number this cube has
     * @param triangles  The triangle on this cube, in this order: up, left, front, right, back, down
     */
    public CachedCube(int identifier, Triangle... triangles) {
        assert triangles.length == 6;

        this.identifier = identifier;

        for (int i = 0; i < 24; i++) {
            Orientation o = Orientation.get(i);
            for (int j = 0; j < 6; j++) {
                if(triangles[j] != Triangle.None) { // Only write if there is a triangle
                    data[i][o.side[j]] = (byte) ((o.triangleOffset[j] + triangles[j].ordinal()) % 4);
                }
            }
        }

        this.triangles = (int) Arrays.stream(triangles).filter(triangle -> triangle != Triangle.None).count();
    }

    /**
     * Sets the orientation of this cube
     */
    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public Triangle getTriangle(Side side) {
        return Triangle.valueOf( data[orientation.ordinal()][side.ordinal()] );
    }

    @Override
    public int getIdentifier() {
        return identifier;
    }
}
