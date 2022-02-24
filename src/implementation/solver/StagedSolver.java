package implementation.solver;

import abstractions.Coordinate;
import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import abstractions.cube.ICubeSet;
import implementation.Puzzle;
import implementation.cube.CubeSorter;
import implementation.solution.DynamicPuzzleSolution;
import tooling.Observer;

import java.util.*;

public class StagedSolver implements IPuzzleSolver, Observer.IObservable {
    /** Immutable */
    public final int dimensionX, dimensionY, dimensionZ;

    /* Mutable */
    private final DynamicPuzzleSolution solution;
    private final boolean[] usedIDs;
    private final CubeSorter sorter;
    private int x = 0, y = 0, z = 0;
    private CubeIterator currentQuery;
    private final LinkedList<Stage> stages = new LinkedList<>();
    private long iter = 0L;

    public StagedSolver(int dimensionX, int dimensionY, int dimensionZ, ICube[] cubes) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.solution = new DynamicPuzzleSolution(dimensionX, dimensionY, dimensionZ);
        this.sorter = new CubeSorter(cubes);
        this.usedIDs = new boolean[cubes.length + 1];
    }

    @Override
    public IPuzzleSolution solve(int _1, int _2, int _3, ICubeSet _4) throws PuzzleNotSolvableException {
        CubeType.checkSolvable(dimensionX, dimensionY, dimensionZ, sorter);

        this.currentQuery = new CubeIterator(this.sorter.matching(this.solution.getFilterAt(x, y, z), i -> true));

        System.out.printf("Starting at (0, 0, 0) with %d possibilities!\n", currentQuery.length());
        if(!currentQuery.hasNext()) throw new PuzzleNotSolvableException();
        this.set();

        while(setNextCoords()) {
            solve();
        }

        System.out.println("Done! Stats:");
        System.out.printf("%d queries cached\n", this.sorter.getSize());
        System.out.printf("Solver finished after %d iterations\n", iter);

        return solution;
    }

    private void solve() throws PuzzleNotSolvableException {
        // x, y, z set here
        //System.out.printf("(Stage %d) Running for coords %d %d %d\n", this.stages.size(), x, y, z);

        if(this.currentQuery == null) {
            this.currentQuery = new CubeIterator(
                    this.sorter.matching(solution.getFilterAt(x, y, z), this::isFree));
            //System.out.println("Current query is empty; Generating new one!");
        }
        //System.out.printf("Current query: %d/ %d elements\n", currentQuery.index + 1, currentQuery.length());
        if(currentQuery.hasNext()) {
            this.set();
        }else { // Nothing found for this step; Stopping and tracing back;
            this.undo();
            this.solve();
        }

        iter++;
        //System.out.println("-----------------------");
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
    public String get1Second() {
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
