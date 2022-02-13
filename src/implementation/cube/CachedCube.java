package implementation.cube;

import abstractions.Orientation;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

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

    @Override
    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public Triangle getTriangle(Side side) {
        return Triangle.valueOf( this.data[this.orientation.ordinal()][side.ordinal()] );
    }

    @Override
    public Triangle getTriangle(Side side, Orientation orientation) {
        return Triangle.valueOf( this.data[orientation.ordinal()][side.ordinal()] );
    }

    @Override
    public int getIdentifier() {
        return this.identifier;
    }

    @Override
    public Stream<Orientation> match(ICubeFilter filter) {
        ArrayList<Orientation> list = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            if(filter.match(this.data[i])) {
                list.add(Orientation.get(i));
            }
        }
        return list.stream();
    }
}
