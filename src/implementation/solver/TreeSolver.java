package implementation.solver;

import abstractions.Coordinate;
import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import abstractions.tree.TreeNode;
import implementation.Puzzle;
import implementation.cube.sorter.ArrayCubeSorter;
import implementation.cube.sorter.CubeSorterFactory;
import implementation.solution.DynamicPuzzleSolution;

import java.util.EnumMap;

public class TreeSolver implements IPuzzleSolver {
    /* Immutable dimensions of this solver */
    public final int dimensionX, dimensionY, dimensionZ;
    /* Immutable, indexed by tree height */
    private final Coordinate[] coords;

    /* Immutable, only change are queries getting cached. Indexed by tree height. */
    private final ArrayCubeSorter[] sorter;

    /* Immutable, only exists for cloning */
    private final EnumMap<CubeType, ArrayCubeSorter> sorterMap = new EnumMap<>(CubeType.class);

    /* Immutable references, but inner state is mutable */
    private final DynamicPuzzleSolution solution;
    private final boolean[] usedIDs;

    /* Mutable */
    private TreeNode node;

    protected TreeSolver(int dimensionX, int dimensionY, int dimensionZ, ICube[] threeEdgeC, ICube[] fourConnectedC, ICube[] fiveC, ICube[] sixC) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.solution = new DynamicPuzzleSolution(dimensionX, dimensionY, dimensionZ);
        this.coords = new LinearCoordinateGenerator(dimensionX, dimensionY, dimensionZ).generate();
        this.sorter = new ArrayCubeSorter[dimensionX * dimensionY * dimensionZ];
        this.usedIDs = new boolean[dimensionX * dimensionY * dimensionZ + 1];

        ArrayCubeSorter threeEdge = CubeSorterFactory.from(threeEdgeC, CubeType.ThreeEdge);
        ArrayCubeSorter fourConnected = CubeSorterFactory.from(fourConnectedC, CubeType.FourConnected);
        ArrayCubeSorter five = CubeSorterFactory.from(fiveC, CubeType.Five);
        ArrayCubeSorter six = CubeSorterFactory.from(sixC, CubeType.Six);

        sorterMap.put(CubeType.ThreeEdge, threeEdge);
        sorterMap.put(CubeType.FourConnected, fourConnected);
        sorterMap.put(CubeType.Five, five);
        sorterMap.put(CubeType.Six, six);

        for (int i = 0; i < this.coords.length; i++) {
            switch (CubeType.get(this.solution.getFilterAt(this.coords[i]).getTriangles())) {
                case ThreeEdge -> this.sorter[i] = threeEdge;
                case FourConnected -> this.sorter[i] = fourConnected;
                case Five -> this.sorter[i] = five;
                case Six -> this.sorter[i] = six;
            }
        }
    }

    /**
     * Internal copy constructor
     */
    private TreeSolver(int dimensionX, int dimensionY, int dimensionZ, Coordinate[] coords, EnumMap<CubeType, ArrayCubeSorter> sorterMap, TreeNode node) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.coords = coords;
        this.sorter = new ArrayCubeSorter[dimensionX * dimensionY * dimensionZ];
        this.solution = new DynamicPuzzleSolution(dimensionX, dimensionY, dimensionZ);
        this.usedIDs = new boolean[dimensionX * dimensionY * dimensionZ + 1];
        this.node = node;

        ArrayCubeSorter threeEdge = sorterMap.get(CubeType.ThreeEdge).clone();
        ArrayCubeSorter fourConnected = sorterMap.get(CubeType.FourConnected).clone();
        ArrayCubeSorter five = sorterMap.get(CubeType.Five).clone();
        ArrayCubeSorter six = sorterMap.get(CubeType.Six).clone();

        sorterMap.put(CubeType.ThreeEdge, threeEdge);
        sorterMap.put(CubeType.FourConnected, fourConnected);
        sorterMap.put(CubeType.Five, five);
        sorterMap.put(CubeType.Six, six);

        for (int i = 0; i < this.coords.length; i++) {
            switch (CubeType.get(this.solution.getFilterAt(this.coords[i]).getTriangles())) {
                case ThreeEdge -> this.sorter[i] = threeEdge;
                case FourConnected -> this.sorter[i] = fourConnected;
                case Five -> this.sorter[i] = five;
                case Six -> this.sorter[i] = six;
            }
        }
    }

    @Override
    public void prepare() {
        this.node = new TreeNode();
    }

    @Override
    public IPuzzleSolution solve() throws PuzzleNotSolvableException {
        return solveConcurrent();
    }

    @Override
    public IPuzzleSolution solveConcurrent() throws PuzzleNotSolvableException {
        int maxHeight = this.coords.length - 1;
        do {
            if(Thread.interrupted()) {
                System.out.println("Got interrupted, exiting!");
                return null;
            }
            expandCurrentNode();
            setNextNode();
        } while(this.node.getHeight() < maxHeight);

        return solution;
    }

    public void expandCurrentNode() {
        if(this.node.isBeingPopulated()) {
            this.node.populate(this.sorter[this.node.getHeight() + 1].matching(this.solution.getFilterAt(this.coords[this.node.getHeight() + 1]), this::isFree));
        }
    }

    /**
     * Traverses the tree down one level.
     */
    public void setNextNode() throws PuzzleNotSolvableException {
        TreeNode nextNode = this.node.getNext();
        if(nextNode == null) { // Either this node is empty, or all children are already being process by another thread. Backtracking it is.
            this.undo();
            this.setNextNode();
        }
        else {
            this.node = nextNode;
            this.solution.set(this.coords[this.node.getHeight()], this.node.getCube());
            this.usedIDs[this.node.getCube().getIdentifier()] = true;
        }
    }

    private void undo() throws PuzzleNotSolvableException {
        // Undoes the operation in the solution object and freeing the id of the used cube
        int id = this.solution.undo();
        if(id == -1) throw new PuzzleNotSolvableException();
        if (Puzzle.DEBUG && id > 0 && !this.usedIDs[id]) {
            throw new IllegalStateException("Trying to free ID " + id + " which wasn't used!");
        }
        this.usedIDs[id] = false; // Skipping 0 check since id=0 isn't used anyway
        this.node.validate();
        this.node = this.node.getParent();
    }

    private boolean isFree(Integer cube) {
        return !this.usedIDs[cube];
    }

    @Override
    public String getCurrentStatus() {
        int m = dimensionX * dimensionY * dimensionZ;
        int h = Math.min(m, this.node.getHeight());
        Coordinate c = this.coords[h];
        return String.format("[%d,%d,%d] Height %d/%d",
                c.x(), c.y(), c.z(), h, m);
    }

    @Override
    public IPuzzleSolver deepClone() {
        return new TreeSolver(dimensionX, dimensionY, dimensionZ, coords, sorterMap, node);
    }

    @Override
    public boolean canRunConcurrent() {
        return true;
    }
}