package implementation.solution;

import implementation.FixedArrayStack;
import abstractions.IPuzzleSolution;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;
import implementation.Puzzle;
import implementation.cube.filter.CubeFilterFactory;

public class DynamicPuzzleSolution implements IPuzzleSolution {
    public final int dimensionX, dimensionY, dimensionZ;
    private final ICube[][][] cubes;
    private final ICubeFilter[][][] filters;
    private final FixedArrayStack<SetOperation> operations;

    public DynamicPuzzleSolution(int dimensionX, int dimensionY, int dimensionZ) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.cubes = new ICube[dimensionX][dimensionY][dimensionZ];
        this.filters = new ICubeFilter[dimensionX][dimensionY][dimensionZ];
        this.operations = new FixedArrayStack<>(new SetOperation[dimensionX*dimensionY*dimensionZ]);

        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    ICubeFilter f = CubeFilterFactory.defaultFilter();
                    if(x == 0) f.setSide(ICube.Side.Back, Triangle.None);
                    if(x == dimensionX - 1) f.setSide(ICube.Side.Front, Triangle.None);
                    if(y == 0) f.setSide(ICube.Side.Left, Triangle.None);
                    if(y == dimensionY - 1) f.setSide(ICube.Side.Right, Triangle.None);
                    if(z == 0) f.setSide(ICube.Side.Down, Triangle.None);
                    if(z == dimensionZ - 1) f.setSide(ICube.Side.Up, Triangle.None);

                    this.filters[x][y][z] = f;
                    //System.out.printf("[%d][%d][%d] Set initial filter: %s\n", x, y, z, f);
                }
            }
        }
    }

    @Override
    public ICube set(int x, int y, int z, ICube cube) {
        if(Puzzle.DEBUG && !this.filters[x][y][z].match(cube)) {
            System.err.printf("[%d][%d][%d] Cube %s doesnt fit %s\n", x, y, z, cube.serialize(), this.filters[x][y][z]);
            throw new IllegalArgumentException("The given cube does not fit!");
        }
        // Setting filters around this cube, so they have to match the cube we set now

        int z2 = z + 1; // Side: Up
        if(validZ(z2) && this.cubes[x][y][z2] == null) {
            this.filters[x][y][z2].setSide((byte) 5, cube.getMatchingTriangle(0, true));
        }
        z2 = z - 1; // Side: Down
        if(validZ(z2) && this.cubes[x][y][z2] == null) {
            this.filters[x][y][z2].setSide((byte) 0, cube.getMatchingTriangle(5, true));
        }
        int y2 = y - 1; // Side: Left
        if(validY(y2) && this.cubes[x][y2][z] == null) {
            this.filters[x][y2][z].setSide((byte) 3, cube.getMatchingTriangle(1, false));
        }
        y2 = y + 1; // Side: Right
        if(validY(y2) && this.cubes[x][y2][z] == null) {
            this.filters[x][y2][z].setSide((byte) 1, cube.getMatchingTriangle(3, false));
        }
        int x2 = x + 1; // Side: Front
        if(validX(x2) && this.cubes[x2][y][z] == null) {
            this.filters[x2][y][z].setSide((byte) 4, cube.getMatchingTriangle(2, false));
        }
        x2 = x - 1; // Side: Back
        if(validX(x2) && this.cubes[x2][y][z] == null) {
            this.filters[x2][y][z].setSide((byte) 2, cube.getMatchingTriangle(4, false));
        }

        // Setting the cube
        ICube tmp = this.cubes[x][y][z];
        this.operations.addLast(new SetOperation(x, y, z, tmp));
        this.cubes[x][y][z] = cube;
        return tmp;
    }

    /**
     * Returns -1 if no operation to undo exists.
     * Returns 0 if the last operation had been undone, but no cube has been freed
     * Returns the id of the cube that has been freed otherwise.
     */
    public int undo() {
        SetOperation op = this.operations.removeLast();
        int x = op.x, y = op.y, z = op.z;

        // Resetting filters

        int z2 = z + 1; // Side: Up
        if(validZ(z2) && this.cubes[x][y][z2] == null) {
            this.filters[x][y][z2].setSide((byte) 5, (byte) 5);
        }
        z2 = z - 1; // Side: Down
        if(validZ(z2) && this.cubes[x][y][z2] == null) {
            this.filters[x][y][z2].setSide((byte) 0, (byte) 5);
        }
        int y2 = y - 1; // Side: Left
        if(validY(y2) && this.cubes[x][y2][z] == null) {
            this.filters[x][y2][z].setSide((byte) 3, (byte) 5);
        }
        y2 = y + 1; // Side: Right
        if(validY(y2) && this.cubes[x][y2][z] == null) {
            this.filters[x][y2][z].setSide((byte) 1, (byte) 5);
        }
        int x2 = x + 1; // Side: Front
        if(validX(x2) && this.cubes[x2][y][z] == null) {
            this.filters[x2][y][z].setSide((byte) 4, (byte) 5);
        }
        x2 = x - 1; // Side: Back
        if(validX(x2) && this.cubes[x2][y][z] == null) {
            this.filters[x2][y][z].setSide((byte) 2, (byte) 5);
        }

        // Resetting cube
        int id = 0;
        if(this.cubes[x][y][z] != null) id = this.cubes[x][y][z].getIdentifier();
        this.cubes[x][y][z] = op.previous;
        return id;
    }

    private boolean validX(int val) {
        return val >= 0 && val < this.dimensionX;
    }

    private boolean validY(int val) {
        return val >= 0 && val < this.dimensionY;
    }

    private boolean validZ(int val) {
        return val >= 0 && val < this.dimensionZ;
    }

    @Override
    public ICube getSolutionAt(int x, int y, int z) {
        return this.cubes[x][y][z];
    }

    @Override
    public ICubeFilter getFilterAt(int x, int y, int z) {
        return this.filters[x][y][z];
    }

    @Override
    public int getDimensionX() {
        return this.dimensionX;
    }

    @Override
    public int getDimensionY() {
        return this.dimensionY;
    }

    @Override
    public int getDimensionZ() {
        return this.dimensionZ;
    }

    /**
     * Contains an atomic operation made on this solution.
     */
    private record SetOperation(int x, int y, int z, ICube previous) {}
}
