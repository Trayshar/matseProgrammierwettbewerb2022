package implementation.solver;

import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.ICubeSet;
import implementation.cube.CubeSorter;
import implementation.solution.DynamicPuzzleSolution;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;

public class StagedSolver implements IPuzzleSolver {
    /** Immutable */
    public final int dimensionX, dimensionY, dimensionZ;

    /* Mutable */
    private final DynamicPuzzleSolution solution;
    private final HashSet<Integer> usedIDs = new HashSet<>();
    private final CubeSorter sorter;
    private final boolean[][][] solved;
    private int x, y, z;
    private Iterator<ICube> currentQuery;
    private final ArrayDeque<Stage> stages = new ArrayDeque<>();

    public StagedSolver(int dimensionX, int dimensionY, int dimensionZ, ICube[] cubes) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.solution = new DynamicPuzzleSolution(dimensionX, dimensionY, dimensionZ);
        this.sorter = new CubeSorter(cubes);
        this.solved = new boolean[dimensionX][dimensionY][dimensionZ];
    }

    @Override
    public IPuzzleSolution solve(int _1, int _2, int _3, ICubeSet _4) throws PuzzleNotSolvableException {
        ICubeFilter f = solution.getFilterAt(0, 0, 0);
        ICube seed = this.sorter.matching(f)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No match found for filter " + f));

        this.set(seed);

        while(setNextCoords()) {
            System.out.printf("Running for coords %d %d %d\n", x, y, z);
            solve();
        }

        return solution;
    }

    private void solve() throws PuzzleNotSolvableException {
        // x, y, z set here

        if(this.currentQuery == null)
            this.currentQuery = this.sorter.matching(solution.getFilterAt(x, y, z))
                    .filter(this::isFree)
                    .iterator();
        if(currentQuery.hasNext()) {
            this.set(currentQuery.next());
        }else { // Nothing found for this step; Stoping and tracing back;
            this.undo();
            this.solve();
        }
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
        if(validX(x) && validY(y) && validZ(z) && !solved[x][y][z]) {
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

    private void set(ICube cube) {
        System.out.printf("[%d][%d][%d] Set %s\n", x, y, z, cube.serialize());
        this.usedIDs.add(cube.getIdentifier());
        this.solved[x][y][z] = true;
        this.solution.set(x, y, z, cube);
        this.stages.addLast(new Stage(x, y, z, currentQuery));
        this.currentQuery = null;
    }

    private void undo() throws PuzzleNotSolvableException {
        Stage g = this.stages.pollLast();
        if(g == null) return;

        int id = this.solution.undo();
        if(id == -1) throw new PuzzleNotSolvableException();
        this.solved[x][y][z] = false;
        if (id != 0 && !this.usedIDs.remove(id)) throw new IllegalStateException("ID " + id + " wasn't used!");
        this.x = g.x;
        this.y = g.y;
        this.z = g.z;
        this.currentQuery = g.results;
    }

    private boolean isFree(ICube cube) {
        return !usedIDs.contains(cube.getIdentifier());
    }

    private record Stage(int x, int y, int z, Iterator<ICube> results) {}
}
