package implementation;

import abstractions.IPuzzleSolution;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;
import implementation.cube.CubeSorter;
import implementation.cube.CubeSorter.QueryResult;
import implementation.cube.filter.CubeFilterFactory;

import java.util.ArrayDeque;
import java.util.HashSet;

@Deprecated
public class MonoSolver {
    /* Immutable */
    public final int dimensionX, dimensionY, dimensionZ;

    /* Mutable. Changed by operations. */
    private final ICube[][][] solution;
    private final ICubeFilter[][][] requirements;
    private final ArrayDeque<SetOperation> operations = new ArrayDeque<>();
    private final HashSet<Integer> usedCubes = new HashSet<>();
    private final CubeSorter sorter;

    public MonoSolver(int dimensionX, int dimensionY, int dimensionZ, ICube[] cubes) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.solution = new ICube[dimensionX][dimensionY][dimensionZ];
        this.requirements = new ICubeFilter[dimensionX][dimensionY][dimensionZ];
        this.sorter = new CubeSorter(cubes);

        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    ICubeFilter f = CubeFilterFactory.from(new byte[]{6, 6, 6, 6, 6, 6});
                    if(x == 0) f.setSide(ICube.Side.Left, Triangle.None);
                    if(x == dimensionX - 1) f.setSide(ICube.Side.Right, Triangle.None);
                    if(y == 0) f.setSide(ICube.Side.Front, Triangle.None);
                    if(y == dimensionY - 1) f.setSide(ICube.Side.Back, Triangle.None);
                    if(z == 0) f.setSide(ICube.Side.Down, Triangle.None);
                    if(z == dimensionZ - 1) f.setSide(ICube.Side.Up, Triangle.None);

                    this.requirements[x][y][z] = f;
                    this.sorter.cache(f);
                }
            }
        }
    }

    public MonoSolution solve() {
        QueryResult seed = this.sorter.matching(requirements[0][0][0]).findAny().orElseThrow();
        //this.set(0, 0, 0, seed);



        return null;
    }

    private boolean isFree(QueryResult r) {
        return !usedCubes.contains(r.cube().getIdentifier());
    }

    private void set(int x, int y, int z, ICube cube) {
        for(ICube.Side s : ICube.Side.values()) {
            int x2 = x + s.x, y2 = y + s.y, z2 = z + s.z;
            if(validX(x2) && validY(y2) && validZ(z2)) {
                this.requirements[x2][y2][z2].setSide(s.getOpposite(), cube.getTriangle(s).getOpposite());
            }
        }

        ICube tmp = this.solution[x][y][z];
        this.operations.addLast(new SetOperation(x, y, z, tmp));
        this.solution[x][y][z] = cube;
        this.usedCubes.add(cube.getIdentifier());
    }

    /**
     * Undo an operation. Returns false if there was no operation to undo.
     */
    private boolean undo() {
        SetOperation op = this.operations.pollLast();
        if (op == null) return false;
        for (ICube.Side s : ICube.Side.values()) {
            int x2 = op.x + s.x, y2 = op.y + s.y, z2 = op.z + s.z;
            if (validX(x2) && validY(y2) && validZ(z2)) {
                this.requirements[x2][y2][z2].setSide(s.getOpposite(), Triangle.AnyNotNone);
            }
        }
        this.solution[op.x][op.y][op.z] = op.previous;
        if(op.previous != null) this.usedCubes.remove(op.previous.getIdentifier());
        return true;
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

    /**
     * Contains an atomic operation.
     */
    private record SetOperation(int x, int y, int z, ICube previous) {}

    /**
     * Solution a MonoSolver may produce
     */
    public record MonoSolution(int dimensionX, int dimensionY, int dimensionZ, ICube[][][] cubes) implements IPuzzleSolution {

        @Override
        public ICube set(int x, int y, int z, ICube cube) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ICube getSolutionAt(int x, int y, int z) {
            return this.cubes[x][y][z];
        }

        @Override
        public ICubeFilter getFilterAt(int x, int y, int z) {
            throw new UnsupportedOperationException();
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
    }
}
