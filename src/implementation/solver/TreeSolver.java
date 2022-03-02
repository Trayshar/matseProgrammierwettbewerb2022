package implementation.solver;

import abstractions.Coordinate;
import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import implementation.Puzzle;
import implementation.cube.sorter.ArrayCubeSorter;
import implementation.cube.sorter.CubeSorterFactory;
import implementation.solution.DynamicPuzzleSolution;

public class TreeSolver implements IPuzzleSolver {
    /* Immutable dimensions of this solver */
    public final int dimensionX, dimensionY, dimensionZ;
    private final Coordinate[] coords;

    /* Immutable, only change are queries getting cached. Indexed by tree height. */
    private final ArrayCubeSorter[] sorter;

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
        this.node = new TreeNode(null);
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
        } while(this.node.height < maxHeight);

        return solution;
    }

    public void expandCurrentNode() throws PuzzleNotSolvableException {
        if(this.node.isBeingPopulated()) {
            this.node.populate(this.sorter[this.node.height + 1].matching(this.solution.getFilterAt(this.coords[this.node.height + 1]), this::isFree));
        }
    }

    /**
     * Traverses the tree down one level.
     * @throws PuzzleNotSolvableException
     */
    public void setNextNode() throws PuzzleNotSolvableException {
        TreeNode nextNode = this.node.getNext();
        if(nextNode == null) { // Either this node is empty, or all children are already being process by another thread. Backtracking it is.
            this.undo();
            this.setNextNode();
        }
        else {
            this.node = nextNode;
            this.solution.set(this.coords[this.node.height], this.node.cube);
            this.usedIDs[this.node.cube.getIdentifier()] = true;
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
        int h = Math.min(m, this.node.height);
        Coordinate c = this.coords[h];
        return String.format("[%d,%d,%d] Height %d/%d",
                c.x(), c.y(), c.z(), h, m);
    }

    @Override
    public IPuzzleSolver deepClone() {
        return null;
    }

    /**
     * Each node has exactly one parent and n children, that are populated on demand later. Only one thread may ever populate the same node at the same time.
     */
    public static class TreeNode {
        public final int height;
        public final ICube cube;
        private final TreeNode parent;
        private TreeNode[] children = null;
        private Status status;

        /**
         * Constructor for the root node which doesn't have a parent.
         */
        public TreeNode(ICube cube) {
            this.cube = cube;
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

        /**
         * Returns the oldest child node of this tree that is not already being populated by another thread, or null if no such node exists
         */
        public TreeNode getNext() {
            for (TreeNode n : this.children) {
                synchronized (n) { // Only one thread at a time may have access to this node
                    if (n.status.eligible) {
                        if(n.status == Status.Empty) n.status = Status.BeingPopulated;
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
        public synchronized void populate(ICube[] cubes) throws PuzzleNotSolvableException {
            assert this.status == Status.BeingPopulated;

            if(cubes.length == 0) {
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
            if(this.children == null) return;
            for (TreeNode n : this.children) {
                synchronized (n) {
                    if (!n.isDead()) return;
                }
            }
            this.setDead();
        }

        private enum Status{
            /** This node has not been explored yet */
            Empty(true),
            /** This node is currently being explored; No access to its inner state until it is populated */
            BeingPopulated(false),
            /** This node has been populated */
            Populated(true),
            /** This node is dead; All references to its children are gone, there is nothing left to do here  */
            Dead(false);

            public final boolean eligible;
            Status(boolean eligible) {
                this.eligible = eligible;
            }
        }
    }
}