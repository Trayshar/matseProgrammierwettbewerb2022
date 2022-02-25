package implementation.solution;

import abstractions.FixedArrayStack;
import abstractions.IPuzzleSolution;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;
import implementation.Puzzle;
import implementation.cube.filter.CubeFilterFactory;

import java.util.Deque;

public class DynamicPuzzleSolution implements IPuzzleSolution {
    public final int dimensionX, dimensionY, dimensionZ;
    private final ICube[][][] cubes;
    private final ICubeFilter[][][] filters;
    private final Deque<SetOperation> operations;

    private final static ICubeFilter defaultFilter = CubeFilterFactory.from(Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone);

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
                    ICubeFilter f = defaultFilter.cloneFilter();
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
        for(ICube.Side s : ICube.Side.values()) {
            int x2 = x + s.x, y2 = y + s.y, z2 = z + s.z;
            if(validX(x2) && validY(y2) && validZ(z2) && this.cubes[x2][y2][z2] == null) {
                this.filters[x2][y2][z2].setSide(s.getOpposite(), cube.getTriangle(s).getMatching(s.z != 0));
            }
        }

        ICube tmp = this.cubes[x][y][z];
        this.operations.addLast(new SetOperation(x, y, z, tmp));
        this.cubes[x][y][z] = cube;
        return tmp;
    }

    public int undo() {
        SetOperation op = this.operations.pollLast();
        if (op == null) return -1;
        for (ICube.Side s : ICube.Side.values()) {
            int x2 = op.x + s.x, y2 = op.y + s.y, z2 = op.z + s.z;
            if (validX(x2) && validY(y2) && validZ(z2) && this.cubes[x2][y2][z2] == null) {
                this.filters[x2][y2][z2].setSide(s.getOpposite(), Triangle.AnyNotNone);
            }
        }
        int id = 0;
        if(this.cubes[op.x][op.y][op.z] != null) id = this.cubes[op.x][op.y][op.z].getIdentifier();
        this.cubes[op.x][op.y][op.z] = op.previous;
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
