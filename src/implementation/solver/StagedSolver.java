package implementation.solver;

import abstractions.Coordinate;
import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.ICube;
import abstractions.cube.ICubeSet;
import implementation.Puzzle;
import implementation.cube.CubeSorter;
import implementation.solution.DynamicPuzzleSolution;

import java.util.*;

public class StagedSolver implements IPuzzleSolver {
    /** Immutable */
    public final int dimensionX, dimensionY, dimensionZ;

    /* Mutable */
    private final DynamicPuzzleSolution solution;
    private final boolean[] usedIDs;
    private final CubeSorter sorter;
    private int x, y, z;
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

        // I'm losing my sanity as I write this
        Coordinate seed = Coordinate.generate(dimensionX, dimensionY, dimensionZ)
                .map(c -> new Object[]{c, this.sorter.matching(solution.getFilterAt(c.x(), c.y(), c.z()), i -> true).length} )
                .min(Comparator.comparingInt(o -> (Integer) o[1]))
                .map(obj -> (Coordinate) obj[0])
                .orElseThrow();

        this.x = seed.x();
        this.y = seed.y();
        this.z = seed.z();
        this.currentQuery = new CubeIterator(this.sorter.matching(this.solution.getFilterAt(x, y, z), i -> true));

        System.out.printf("Staring at %s with %d possibilities!\n", seed, currentQuery.length());
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
     * I HATE THIS
     */
    private boolean setNextCoords() {
        for (int dx = 0; dx < dimensionX; dx++) {
            for (int dy = 0; dy < dimensionY; dy++) {
                for (int dz = 0; dz < dimensionZ; dz++) {
                    if(setAndReturnCoords(x + dx, y + dy, z + dz)) return true;
                    if(setAndReturnCoords(x - dx, y + dy, z + dz)) return true;
                    if(setAndReturnCoords(x + dx, y - dy, z + dz)) return true;
                    if(setAndReturnCoords(x - dx, y - dy, z + dz)) return true;
                    if(setAndReturnCoords(x + dx, y + dy, z - dz)) return true;
                    if(setAndReturnCoords(x - dx, y + dy, z - dz)) return true;
                    if(setAndReturnCoords(x + dx, y - dy, z - dz)) return true;
                    if(setAndReturnCoords(x - dx, y - dy, z - dz)) return true;
                }
            }
        }

        return false;
    }

    private boolean setAndReturnCoords(int x, int y, int z) {
        if(validX(x) && validY(y) && validZ(z) && this.solution.getSolutionAt(x, y, z) == null) {
            this.x = x;
            this.y = y;
            this.z = z;
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
