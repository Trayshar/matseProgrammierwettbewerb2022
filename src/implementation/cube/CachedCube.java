package implementation.cube;

import abstractions.Orientation;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;
import implementation.Puzzle;

import java.util.ArrayList;
import java.util.Arrays;

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
    /** The unique id of this cube */
    public final int uniqueCubeId;

    private static final int[] pow5 = {1, 5, 25, 125, 625, 3125};

    /**
     * @param identifier The unique number this cube has
     * @param triangles  The triangle on this cube, in this order: up, left, front, right, back, down
     */
    public CachedCube(int identifier, Triangle... triangles) {
        assert triangles.length == 6;

        this.data = new byte[24][6];
        this.identifier = identifier;

        int id = Integer.MAX_VALUE;
        for (int i = 0; i < 24; i++) { // For each of the 24 orientations
            int tmpId = 0;
            Orientation o = Orientation.get(i);
            for (int j = 0; j < 6; j++) { // For each of the 6 sides of the cube
                if(triangles[j] != Triangle.None) { // Only write if there is a triangle
                    byte triangle  = (byte) (o.triangleOffset[j] + triangles[j].ordinal()); // Calculate the triangle on this side
                    if (triangle > 4) triangle -= 4; // Normalise it
                    data[i][o.side[j]] = triangle; // Set it
                    tmpId += triangle * pow5[j]; // Apply the formula for ID calculation: sum(0 ≤ i < 6 ): side[i] * 5^i
                    if(Puzzle.DEBUG && data[i][o.side[j]] == 0) {
                        data[i][o.side[j]] = 1;
                        System.out.printf("[%s] Illegal rotation operation (%d, %d)\n", this.identifier, o.triangleOffset[j], triangles[j].ordinal());
                    }
                }
            }
            if(tmpId < id) id = tmpId;
        }

        this.uniqueCubeId = id;
        this.triangles = (int) Arrays.stream(triangles).filter(triangle -> triangle != Triangle.None).count();
    }

    private CachedCube(byte[][] data, Orientation orientation, int identifier, int triangles, int uniqueCubeId) {
        this.data = data;
        this.orientation = orientation;
        this.identifier = identifier;
        this.triangles = triangles;
        this.uniqueCubeId = uniqueCubeId;
    }

    @Override
    protected CachedCube clone() {
        try {
            return (CachedCube) super.clone();
        } catch (CloneNotSupportedException e) { // shouldn't happen
            System.err.println("Couldn't natively clone CachedCube: " + e.getLocalizedMessage());
            return new CachedCube(this.data, this.orientation, this.identifier, this.triangles, this. uniqueCubeId);
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
    public int getUniqueCubeId() {
        return this.uniqueCubeId;
    }

    @Override
    public ArrayList<Orientation> match(ICubeFilter filter) {
        ArrayList<Orientation> list = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            if(filter.match(this.data[i])) {
                list.add(Orientation.get(i));
            }
        }
        return list;
    }

    @Override
    public byte[] getTriangles() {
        return this.data[this.orientation.ordinal()];
    }

    @Override
    public byte[] getTriangles(Orientation o) {
        return this.data[o.ordinal()];
    }

    @Override
    public String toString() {
        return this.serialize();
    }

    @Override
    public byte getMatchingTriangle(int side, boolean isVertical) {
        return Triangle.getMatching(this.data[this.orientation.ordinal()][side], isVertical);
    }
}
