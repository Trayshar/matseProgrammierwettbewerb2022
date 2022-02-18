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
    /** IMMUTABLE */
    private final byte[][] data;
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

        this.data = new byte[24][6];
        this.identifier = identifier;

        for (int i = 0; i < 24; i++) {
            Orientation o = Orientation.get(i);
            for (int j = 0; j < 6; j++) {
                if(triangles[j] != Triangle.None) { // Only write if there is a triangle
                    byte tmp  = (byte) (o.triangleOffset[j] + triangles[j].ordinal());
                    if (tmp > 4) tmp -= 4;
                    data[i][o.side[j]] = tmp;
//                    if(data[i][o.side[j]] == 0) {
//                        data[i][o.side[j]] = 1;
//                        System.out.printf("[%s] Illegal rotation operation (%d, %d)\n", this.identifier, o.triangleOffset[j], triangles[j].ordinal());
//                    }
                }
            }
        }

        this.triangles = (int) Arrays.stream(triangles).filter(triangle -> triangle != Triangle.None).count();
    }

    private CachedCube(byte[][] data, Orientation orientation, int identifier, int triangles) {
        this.data = data;
        this.orientation = orientation;
        this.identifier = identifier;
        this.triangles = triangles;
    }

    @Override
    public CachedCube clone() {
        try {
            return (CachedCube) super.clone();
        } catch (CloneNotSupportedException e) { // shouldn't happen
            System.err.println("Couldn't natively clone CachedCube: " + e.getLocalizedMessage());
            return new CachedCube(this.data, this.orientation, this.identifier, this.triangles);
        }
    }

    @Override
    public ICube cloneCube() {
        return this.clone();
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
    public int getNumTriangles() {
        return this.triangles;
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

    @Override
    public String toString() {
        return this.serialize();
    }
}
