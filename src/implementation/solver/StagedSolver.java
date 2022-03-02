package implementation.solver;

import abstractions.cube.CubeType;
import implementation.FixedArrayStack;
import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.ICube;
import implementation.Puzzle;
import implementation.cube.sorter.ArrayCubeSorter;
import implementation.cube.sorter.CubeSorterFactory;
import implementation.solution.DynamicPuzzleSolution;

import java.util.HashMap;

public class StagedSolver implements IPuzzleSolver {
    /* Immutable */
    public final int dimensionX, dimensionY, dimensionZ;

    /* Mutable */
    private final DynamicPuzzleSolution solution;
    private final boolean[] usedIDs;
    private final boolean[][][] solved;
    private final ArrayCubeSorter[][][] sorter;
    private int x = 0, y = 0, z = 0;
    private CubeIterator currentQuery;
    private final FixedArrayStack<Stage> stages;
    private long iter = 0L;

    /**
     * Internal base constructor
     */
    private StagedSolver(int dimensionX, int dimensionY, int dimensionZ, int cubeLength) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.solution = new DynamicPuzzleSolution(dimensionX, dimensionY, dimensionZ);
        this.sorter = new ArrayCubeSorter[dimensionX][dimensionY][dimensionZ];
        this.usedIDs = new boolean[cubeLength + 1];
        this.stages = new FixedArrayStack<>(new Stage[dimensionX * dimensionY * dimensionZ]);
        this.solved = new boolean[dimensionX][dimensionY][dimensionZ];
    }

    /**
     * Internal copy constructor
     */
    private StagedSolver(int dimensionX, int dimensionY, int dimensionZ, int cubeLength, ArrayCubeSorter[][][] sorter) {
        this(dimensionX, dimensionY, dimensionZ, cubeLength);

        HashMap<ArrayCubeSorter, ArrayCubeSorter> oldToNew = new HashMap<>();
        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    this.sorter[x][y][z] = oldToNew.computeIfAbsent(sorter[x][y][z], ArrayCubeSorter::clone);
                }
            }
        }
    }

    /**
     * "Line" type
     */
    protected StagedSolver(int dimensionX, int dimensionY, int dimensionZ, ICube[] oneC, ICube[] twoOppositeC) {
        this(dimensionX, dimensionY, dimensionZ, oneC.length + twoOppositeC.length);

        ArrayCubeSorter one = CubeSorterFactory.from(oneC, CubeType.One);
        ArrayCubeSorter twoOpposite = CubeSorterFactory.from(twoOppositeC, CubeType.TwoOpposite);
        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    this.sorter[x][y][z] = CubeType.get(this.solution.getFilterAt(x,y,z).getTriangles()) == CubeType.One ? one : twoOpposite;
                }
            }
        }
    }

    /**
     * "Plane" type
     */
    protected StagedSolver(int dimensionX, int dimensionY, int dimensionZ, ICube[] twoConnectedC, ICube[] threeConnectedC, ICube[] fourRoundC) {
        this(dimensionX, dimensionY, dimensionZ, twoConnectedC.length + threeConnectedC.length + fourRoundC.length);

        ArrayCubeSorter twoConnected = CubeSorterFactory.from(twoConnectedC, CubeType.TwoConnected);
        ArrayCubeSorter threeConnected = CubeSorterFactory.from(threeConnectedC, CubeType.ThreeConnected);
        ArrayCubeSorter fourRound = CubeSorterFactory.from(fourRoundC, CubeType.FourRound);

        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    switch (CubeType.get(this.solution.getFilterAt(x,y,z).getTriangles())) {
                        case TwoConnected -> this.sorter[x][y][z] = twoConnected;
                        case ThreeConnected -> this.sorter[x][y][z] = threeConnected;
                        case FourRound -> this.sorter[x][y][z] = fourRound;
                    }
                }
            }
        }
    }

    /**
     * "Cuboid" type
     */
    protected StagedSolver(int dimensionX, int dimensionY, int dimensionZ, ICube[] threeEdgeC, ICube[] fourConnectedC, ICube[] fiveC, ICube[] sixC) {
        this(dimensionX, dimensionY, dimensionZ, threeEdgeC.length + fourConnectedC.length + fiveC.length + sixC.length);

        ArrayCubeSorter threeEdge = CubeSorterFactory.from(threeEdgeC, CubeType.ThreeEdge);
        ArrayCubeSorter fourConnected = CubeSorterFactory.from(fourConnectedC, CubeType.FourConnected);
        ArrayCubeSorter five = CubeSorterFactory.from(fiveC, CubeType.Five);
        ArrayCubeSorter six = CubeSorterFactory.from(sixC, CubeType.Six);

        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    switch (CubeType.get(this.solution.getFilterAt(x,y,z).getTriangles())) {
                        case ThreeEdge -> this.sorter[x][y][z] = threeEdge;
                        case FourConnected -> this.sorter[x][y][z] = fourConnected;
                        case Five -> this.sorter[x][y][z] = five;
                        case Six -> this.sorter[x][y][z] = six;
                    }
                }
            }
        }
    }

    public void prepare() throws PuzzleNotSolvableException {
        this.currentQuery = new CubeIterator(this.sorter[0][0][0].matching(this.solution.getFilterAt(x, y, z)));

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
        while(setNextCoords()) {
            if(Thread.currentThread().isInterrupted()) {
                System.out.println("Got interrupted, exiting!");
                return null;
            }
            solveInternally();
        }

        return solution;
    }

    private void solveInternally() throws PuzzleNotSolvableException {
        // x, y, z set here
        if(this.currentQuery == null) {
            this.currentQuery = new CubeIterator(
                    this.sorter[x][y][z].matching(solution.getFilterAt(x, y, z), this::isFree));
        }
        if(currentQuery.hasNext()) {
            this.set();
        }else { // Nothing found for this step; Stopping and tracing back;
            this.undo();
            this.solveInternally();
        }

        iter++;
    }

    private int sols = 0;

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

        System.out.printf("- Found solution %d -\n", sols++);
        return Puzzle.DEBUG; // Only returns the first solution if DEBUG is false; Look for other solutions otherwise
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
        Stage zero = this.stages.peekFirst();
        Stage last = this.stages.peekLast();

        String lastStr = " (?/?)", zeroStr = ", StageZero(?/?)";
        if(zero != null) zeroStr = ", StageZero(" + zero.results.index + "/" + zero.results.length() + ")";
        if(last != null) lastStr = " (" + last.results.index + "/" + last.results.length() + ")";
        return String.format("[%d] [%d,%d,%d] Stage %d%s%s",
                iter, x, y, z, this.stages.size(), lastStr, zeroStr);
    }

    @Override
    public IPuzzleSolver deepClone() {
        StagedSolver s = new StagedSolver(dimensionX, dimensionY, dimensionZ, usedIDs.length-1, this.sorter);
        for (Stage stage : this.stages) {
            stage.clone();
            s.stages.addLast(stage.clone());
        }
        return s;
    }

    private record Stage(int x, int y, int z, CubeIterator results) implements Cloneable {
        @Override
        public Stage clone() {
            return new Stage(x, y, z, results.clone());
        }
    }

    private static class CubeIterator implements Cloneable{
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

        @Override
        public CubeIterator clone() {
            try {
                return (CubeIterator) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }
}
