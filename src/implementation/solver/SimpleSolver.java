package implementation.solver;

import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.ICube;
import abstractions.cube.ICubeSorter;
import implementation.FixedArrayStack;
import implementation.Puzzle;
import implementation.cube.sorter.CubeSorterFactory;
import implementation.solution.DynamicPuzzleSolution;

/**
 * A simple, linear solver for small problems.
 */
public class SimpleSolver implements IPuzzleSolver {
    /* Immutable */
    public final int dimensionX, dimensionY, dimensionZ;

    /* Mutable */
    private final DynamicPuzzleSolution solution;
    private final boolean[] usedIDs;
    private final boolean[][][] solved;
    private final ICubeSorter sorter;
    private int x = 0, y = 0, z = 0;
    private CubeIterator currentQuery;
    private final FixedArrayStack<Stage> stages;

    protected SimpleSolver(int dimensionX, int dimensionY, int dimensionZ, ICube[] cubes) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.solution = new DynamicPuzzleSolution(dimensionX, dimensionY, dimensionZ);
        this.sorter = CubeSorterFactory.makeHashCubeSorter(cubes);
        this.usedIDs = new boolean[cubes.length + 1];
        this.stages = new FixedArrayStack<>(new Stage[dimensionX * dimensionY * dimensionZ]);
        this.solved = new boolean[dimensionX][dimensionY][dimensionZ];
    }

    public void prepare() throws PuzzleNotSolvableException {
        this.currentQuery = new CubeIterator(this.sorter.matching(this.solution.getFilterAt(x, y, z)));

        System.out.printf("Starting at (0, 0, 0) with %d possibilities!\n", currentQuery.length());
        if(!currentQuery.hasNext()) throw new PuzzleNotSolvableException();
        this.set();
    }

    @Override
    public IPuzzleSolution solve() throws PuzzleNotSolvableException {
        while(setNextCoords()) {
            solveInternally();
        }

        return solution;
    }

    @Override
    public IPuzzleSolution solveConcurrent() throws PuzzleNotSolvableException {
        return solve();
    }

    private void solveInternally() throws PuzzleNotSolvableException {
        // x, y, z set here
        if(this.currentQuery == null) {
            this.currentQuery = new CubeIterator(
                    this.sorter.matching(solution.getFilterAt(x, y, z), this::isFree));
        }
        if(currentQuery.hasNext()) {
            this.set();
        }else { // Nothing found for this step; Stopping and tracing back;
            this.undo();
            this.solveInternally();
        }
    }

    /**
     * Goes in x direction till the end, then y, then z.
     */
    private boolean setNextCoords() {
        if(validX(x + 1) && !this.solved[x+1][y][z]) {
            this.x++;
            return true;
        }else if(validY(y + 1) && !this.solved[0][y+1][z]) {
            this.x = 0;
            this.y++;
            return true;
        }else if(validZ(z + 1) && !this.solved[0][0][z+1]) {
            this.x = 0;
            this.y = 0;
            this.z++;
            return true;
        }

        return false;
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

    private void set() {
        ICube cube = currentQuery.next();
        this.usedIDs[cube.getIdentifier()] = true;
        this.solved[x][y][z] = true;
        this.solution.set(x, y, z, cube);
        this.stages.addLast(new Stage(x, y, z, currentQuery));
        this.currentQuery = null;
    }

    private void undo() throws PuzzleNotSolvableException {
        // Retrieves and removes the last stage
        Stage g = this.stages.pollLast();
        if(g == null) throw new PuzzleNotSolvableException();

        // Undoes the operation in the solution object and freeing the id of the used cube
        int id = this.solution.undo();
        if(id == -1) throw new PuzzleNotSolvableException();
        if (Puzzle.DEBUG && id > 0 && !this.usedIDs[id]) {
            throw new IllegalStateException("Trying to free ID " + id + " which wasn't used!");
        }
        this.usedIDs[id] = false; // Skipping 0 check since id=0 isn't used anyway
        this.solved[x][y][z] = false;

        this.x = g.x;
        this.y = g.y;
        this.z = g.z;
        this.currentQuery = g.results;
    }

    private boolean isFree(Integer cube) {
        return !this.usedIDs[cube];
    }

    @Override
    public String getCurrentStatus() {
        return "";
    }

    @Override
    public IPuzzleSolver deepClone() {
        throw new UnsupportedOperationException();
    }

    private record Stage(int x, int y, int z, CubeIterator results) {}

    private static class CubeIterator {
        private final ICube[] cubes;
        private int index = 0;

        private CubeIterator(ICube[] cubes) {
            this.cubes = cubes;
        }

        public boolean hasNext() {
            return index < cubes.length;
        }

        public int length() {
            return cubes.length;
        }

        public ICube next() {
            return cubes[index++];
        }
    }
}
