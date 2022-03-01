package implementation.solver;

import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.ICube;
import implementation.cube.sorter.ArrayCubeSorter;
import implementation.solution.DynamicPuzzleSolution;

public class TreeSolver implements IPuzzleSolver {
    /* Immutable dimensions of this solver */
    public final int dimensionX, dimensionY, dimensionZ;

    /* Immutable, only change are queries getting cached. */
    private final ArrayCubeSorter[][][] sorter;

    /* Immutable references, but inner state is mutable */
    private final DynamicPuzzleSolution solution;
    private final boolean[] usedIDs;

    protected TreeSolver(int dimensionX, int dimensionY, int dimensionZ, ArrayCubeSorter[][][] sorter, int cubeCount) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.solution = new DynamicPuzzleSolution(dimensionX, dimensionY, dimensionZ);
        this.sorter = sorter;
        this.usedIDs = new boolean[cubeCount + 1];
    }

    @Override
    public void prepare() throws PuzzleNotSolvableException {}

    @Override
    public IPuzzleSolution solve() throws PuzzleNotSolvableException {
        return solveConcurrent();
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

    public boolean setNextNode() {

    }

    @Override
    public String getCurrentStatus() {
        return null;
    }

    @Override
    public IPuzzleSolver deepClone() {
        return null;
    }

    /**
     * Each node has exactly one parent and n children, that are populated on demand later. Only one thread may ever populate the same node at the same time.
     */
    public static class TreeNode {
        public final int x, y, z, height;
        public final ICube cube;
        private final TreeNode parent;
        private TreeNode[] children = null;
        private Status status = Status.Empty;

        /**
         * Constructor for the root node which doesn't have a parent.
         */
        public TreeNode(int x, int y, int z, ICube cube) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.cube = cube;
            this.parent = new NullNode();
            this.height = 0;
        }

        /**
         * Internal constructor for creating a child node
         */
        private TreeNode(TreeNode parent, int x, int y, int z, ICube cube) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.cube = cube;
            this.parent = parent;
            this.height = parent.height + 1;
        }

        /**
         * Returns the oldest child node of this tree that is not already being populated by another thread, or null if no such node exists
         */
        public TreeNode getChildren() {
            for (TreeNode n : this.children) {
                synchronized (n) {
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
        public void populate(int x, int y, int z, ICube[] cubes) throws PuzzleNotSolvableException {
            assert this.status == Status.BeingPopulated;

            if(cubes.length == 0) {
                setDead();
                return;
            }

            this.children = new TreeNode[cubes.length];
            for (int i = 0; i < cubes.length; i++) {
                this.children[i] = new TreeNode(this, x, y, z, cubes[i]);
            }
            this.status = Status.Populated;
        }

        /**
         * Returns whether this node has been populated already.
         */
        public boolean isPopulated() {
            return this.status == Status.Populated;
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
        public void setDead() throws PuzzleNotSolvableException {
            this.children = new TreeNode[0];
            this.status = Status.Dead;
            this.parent.validate();
        }

        protected void validate() throws PuzzleNotSolvableException {
            for (TreeNode n : this.children) {
                if (!n.isDead()) return;
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

        private static class NullNode extends TreeNode {
            /**
             * Constructor for the null node; A node that shouldn't exist.
             */
            public NullNode() {
                super(-1, -1, -1, null); //FIXME: Circular reference
            }

            @Override
            public TreeNode getChildren() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void populate(int x, int y, int z, ICube[] cubes) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isDead() {
                return true;
            }

            @Override
            public boolean isPopulated() {
                return true;
            }

            @Override
            public void setDead() throws PuzzleNotSolvableException {
                throw new UnsupportedOperationException();
            }

            @Override
            protected void validate() throws PuzzleNotSolvableException {
                throw new PuzzleNotSolvableException("Last node died!");
            }
        }
    }
}