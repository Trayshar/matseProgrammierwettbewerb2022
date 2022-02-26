package implementation.solver;

import abstractions.FixedArrayStack;
import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.ICube;
import abstractions.cube.ICubeSorter;
import implementation.Puzzle;
import implementation.cube.sorter.CubeSorterFactory;
import implementation.solution.DynamicPuzzleSolution;

public class StagedSolver implements IPuzzleSolver {
    /* Immutable */
    public final int dimensionX, dimensionY, dimensionZ;

    /* Mutable */
    private final DynamicPuzzleSolution solution;
    private final boolean[] usedIDs;
    private final ICubeSorter sorter;
    private int x = 0, y = 0, z = 0;
    private CubeIterator currentQuery;
    private final FixedArrayStack<Stage> stages;
    private long iter = 0L;

    protected StagedSolver(int dimensionX, int dimensionY, int dimensionZ, ICube[] cubes) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.solution = new DynamicPuzzleSolution(dimensionX, dimensionY, dimensionZ);
        this.sorter = CubeSorterFactory.from(cubes);
        this.usedIDs = new boolean[cubes.length + 1];
        this.stages = new FixedArrayStack<>(new Stage[dimensionX*dimensionY*dimensionZ]);
    }

    private void prepare() throws PuzzleNotSolvableException {
        SolverFactory.checkSolvable(dimensionX, dimensionY, dimensionZ, sorter);

        this.currentQuery = new CubeIterator(this.sorter.matching(this.solution.getFilterAt(x, y, z)));

        System.out.printf("Starting at (0, 0, 0) with %d possibilities!\n", currentQuery.length());
        if(!currentQuery.hasNext()) throw new PuzzleNotSolvableException();
        this.set();
    }

    @Override
    public IPuzzleSolution solve() throws PuzzleNotSolvableException {
        this.prepare();

        while(setNextCoords()) {
            solveInternally();
        }

        this.done();

        return solution;
    }

    @Override
    public IPuzzleSolution solveConcurrent() throws PuzzleNotSolvableException {
        this.prepare();

        while(setNextCoords()) {
            if(Thread.currentThread().isInterrupted()) {
                System.out.println("Got interrupted, exiting!");
                return null;
            }
            solveInternally();
        }

        this.done();

        return solution;
    }

    private void done() {
        System.out.println("Done! Stats:");
        System.out.printf("%d queries cached\n", this.sorter.getNumCachedQueries());
        System.out.printf("Solver finished after %d iterations\n", iter);
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

        iter++;
    }

    /**
     * Goes in x direction till the end, then y, then z.
     */
    private boolean setNextCoords() {
        if(validX(x + 1) && this.solution.getSolutionAt(x + 1, y, z) == null) {
            this.x++;
            return true;
        }else if(validY(y + 1) && this.solution.getSolutionAt(0, y + 1, z) == null) {
            this.x = 0;
            this.y++;
            return true;
        }else if(validZ(z + 1) && this.solution.getSolutionAt(0, 0, z + 1) == null) {
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
        //System.out.printf("[%d][%d][%d] Set cube: %s\n", x, y, z, cube.serialize());
        this.usedIDs[cube.getIdentifier()] = true;
        this.solution.set(x, y, z, cube);
        this.stages.addLast(new Stage(x, y, z, currentQuery));
        this.currentQuery = null;
    }

    private void undo() throws PuzzleNotSolvableException {
        Stage g = this.stages.pollLast();
        if(g == null) throw new PuzzleNotSolvableException();

        int id = this.solution.undo();
        if(id == -1) throw new PuzzleNotSolvableException();
        if (id > 0) {
            if(Puzzle.DEBUG && !this.usedIDs[id]) throw new IllegalStateException("Trying to free ID " + id + " which wasn't used!");
            this.usedIDs[id] = false;
        }

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
        Stage zero = this.stages.peekFirst();
        Stage last = this.stages.peekLast();

        String lastStr = " (?/?)", zeroStr = ", StageZero(?/?)";
        if(zero != null) zeroStr = ", StageZero(" + zero.results.index + "/" + zero.results.length() + ")";
        if(last != null) lastStr = " (" + last.results.index + "/" + last.results.length() + ")";
        return String.format("[%d] [%d,%d,%d] Stage %d%s%s",
                iter, x, y, z, this.stages.size(), lastStr, zeroStr);
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
