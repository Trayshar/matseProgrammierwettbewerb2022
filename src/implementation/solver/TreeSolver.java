package implementation.solver;

import abstractions.*;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import abstractions.cube.Triangle;
import implementation.Puzzle;
import implementation.cube.sorter.ArrayCubeSorter;
import implementation.cube.sorter.CubeSorterFactory;
import implementation.solution.DynamicPuzzleSolution;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;

public class TreeSolver implements IPuzzleSolver {
    /* Immutable dimensions of this solver */
    public final int dimensionX, dimensionY, dimensionZ;
    /* Immutable, indexed by tree height */
    private final Coordinate[] coords;
    /* Immutable feature flag. */
    private final boolean enableLookAhead = false;

    /* Immutable, only change are queries getting cached. Indexed by tree height. */
    private final ArrayCubeSorter[] sorter;

    /* Immutable, only exists for cloning */
    private final EnumMap<CubeType, ArrayCubeSorter> sorterMap = new EnumMap<>(CubeType.class);

    /* Immutable references, but inner state is mutable */
    private final DynamicPuzzleSolution solution;
    private final boolean[] usedIDs;

    /* Mutable */
    private TreeNode node;
    private long sets = 0, expands = 0, undos = 0;

    protected TreeSolver(int dimensionX, int dimensionY, int dimensionZ, ICube[] threeEdgeC, ICube[] fourConnectedC, ICube[] fiveC, ICube[] sixC, CoordinateGenerator generator) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.solution = new DynamicPuzzleSolution(dimensionX, dimensionY, dimensionZ);
        this.coords = generator.generate();
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
                if(Puzzle.LOG) System.out.println("Got interrupted, exiting!");
                return null;
            }
            expandCurrentNode();
            setNextNode();
        } while(this.node.getHeight() < maxHeight);

        return solution;
    }

    public void expandCurrentNode() {
        if(this.node.isBeingPopulated()) {
            int childHeight = this.node.getHeight() + 1;
            ICube[] cubes = this.sorter[childHeight].matching(this.solution.getFilterAt(this.coords[childHeight]), this::isFree);

            if(enableLookAhead && childHeight + 1 < this.coords.length && cubes.length > 3) { // Lookahead to sort child notes
                ICube.Side side = this.coords[childHeight + 1].adjacentTo(this.coords[childHeight]); // Side of "childHeight" that looks at "childHeight + 1"
                if(side != null) {
                    var sort = this.sorter[childHeight + 1];
                    var filter = this.solution.getFilterAt(this.coords[childHeight + 1]).cloneFilter();
                    var opposite = side.getOpposite();
                    boolean isVertical = side.z != 0;
                    int[] comp = new int[4]; // Maps the triangle a cube has on side "side" to the amount of potential next candidates
                    for (int i = 0; i < 4; i++) {
                        filter.setSide(opposite, Triangle.valueOf(i+1).getMatching(isVertical));
                        comp[i] = sort.count(filter);
                    }

                    Arrays.sort(cubes, (cube1, cube2) -> {
                        int count1 = comp[cube1.getTriangle(side).ordinal() - 1];
                        int count2 = comp[cube2.getTriangle(side).ordinal() - 1];

                        if(count1 == 0 && count2 > 0) return 1;
                        if(count1 == count2) return 0;
                        if(count1 > 0 && count2 == 0) return -1;
                        if(count1 < count2) return -1;
                        return 1; // count1 > count2
                    });
                }
            }
            this.node.populate(cubes);
            expands++;
        }
    }

    /**
     * Traverses the tree down one level.
     */
    public void setNextNode() throws PuzzleNotSolvableException {
        TreeNode nextNode = this.node.getNext();
        if(nextNode == null) { // Either this node is empty, or all children are already being processed by another thread
            this.undo();
            this.setNextNode();
        }
        else {
            if(Puzzle.DEBUG && nextNode.isDead()) throw new ConcurrentModificationException();
            this.node = nextNode;
            ICube tmp = this.solution.set(this.coords[this.node.getHeight()], this.node.getCube());
            if(Puzzle.DEBUG && tmp != null) throw new IllegalStateException();
            this.usedIDs[this.node.getCube().getIdentifier()] = true;
            this.sets++;
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
        this.undos++;
    }

    private boolean isFree(Integer cube) {
        return !this.usedIDs[cube];
    }

    private long old_sets = 0, old_expands = 0, old_undos = 0;

    @Override
    public String getCurrentStatus() {
        long diff_sets = sets - old_sets;
        long diff_expands = expands - old_expands;
        long diff_undos = undos - old_undos;

        old_sets = sets;
        old_expands = expands;
        old_undos = undos;

        int m = dimensionX * dimensionY * dimensionZ;
        int h = Math.min(m, this.node.getHeight());
        Coordinate c = this.coords[h];
        return String.format("[%d,%d,%d] Height %d/%d with [%d set, %d expand, %d undo] per second",
                c.x(), c.y(), c.z(), h, m, diff_sets, diff_expands, diff_undos);
    }

    @Override
    public IPuzzleSolver deepClone() {
        return new TreeSolver(dimensionX, dimensionY, dimensionZ, coords, sorterMap, node);
    }

    @Override
    public boolean canRunConcurrent() {
        return true;
    }

    /**
     * Each node has exactly one parent and n children, that are populated on demand later. Only one thread may ever populate the same node at the same time.
     */
    public static class TreeNode {
        private final int height;
        private final ICube cube;
        private final TreeNode parent;
        private TreeNode[] children = null;
        private Status status;

        /**
         * Constructor for the root node which doesn't have a parent.
         */
        public TreeNode() {
            this.cube = null;
            this.parent = null;
            this.height = -1;
            this.status = Status.BeingPopulated;
        }

        /**
         * Internal constructor for creating a child node
         */
        private TreeNode(TreeNode parent, ICube cube) {
            this.cube = cube;
            this.parent = parent;
            this.height = parent.height + 1;
            this.status = Status.Empty;
        }

        public TreeNode getParent() {
            return parent;
        }

        public int getHeight() {
            return height;
        }

        public ICube getCube() {
            return cube;
        }

        /**
         * Returns the oldest child node of this tree that is not already being populated by another thread, or null if no such node exists
         */
        public TreeNode getNext() {
            for (TreeNode n : this.children) {
                synchronized (n) { // Only one thread at a time may have access to this node
                    if (n.status.eligible) {
                        if (n.status == Status.Empty) n.status = Status.BeingPopulated;
                        return n;
                    }
                }
            }
            return null;
        }

        /**
         * Populates this node with the given values. The next generation will consist of as many nodes as values are provided.
         * If no values are provided the node is marked as dead.
         * This function may only be called on this node if it is currently being populated.
         */
        public synchronized void populate(ICube[] cubes) {
            assert this.status == Status.BeingPopulated;

            if (cubes.length == 0) {
                setDead();
                return;
            }

            this.children = new TreeNode[cubes.length];
            for (int i = 0; i < cubes.length; i++) {
                this.children[i] = new TreeNode(this, cubes[i]);
            }
            this.status = Status.Populated;
        }

        /**
         * Returns whether this node is being populated.
         */
        public boolean isBeingPopulated() {
            return this.status == Status.BeingPopulated;
        }

        /**
         * Returns whether this node is dead, meaning it doesn't have any live children.
         */
        public boolean isDead() {
            return this.status == Status.Dead;
        }

        /**
         * Marks this node as dead. All references to its children will be removed and its parent will be notified of its death.
         */
        protected void setDead() {
            synchronized (this) {
                this.children = new TreeNode[0];
                this.status = Status.Dead;
            }
        }

        /**
         * Checks whether this node is still alive
         */
        public synchronized void validate() {
            if (this.children == null) return;
            for (TreeNode n : this.children) {
                synchronized (n) { //TODO: Potentially useless lock
                    if (!n.isDead()) return;
                }
            }
            this.setDead();
        }

        private enum Status {
            /**
             * This node has not been explored yet
             */
            Empty(true),
            /**
             * This node is currently being explored; No access to its inner state until it is populated
             */
            BeingPopulated(false),
            /**
             * This node has been populated
             */
            Populated(true),
            /**
             * This node is dead; All references to its children are gone, there is nothing left to do here
             */
            Dead(false);

            public final boolean eligible;

            Status(boolean eligible) {
                this.eligible = eligible;
            }
        }
    }
}