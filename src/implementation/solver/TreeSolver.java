package implementation.solver;

import abstractions.*;
import abstractions.cube.*;
import implementation.Puzzle;
import implementation.cube.filter.CubeFilterFactory;
import implementation.cube.sorter.ArrayCubeSorter;
import implementation.cube.sorter.CubeSorterFactory;

import java.util.ConcurrentModificationException;
import java.util.EnumMap;

public class TreeSolver implements IPuzzleSolver, IPuzzleSolution {
    /* Immutable dimensions of this solver */
    public final int dimensionX, dimensionY, dimensionZ;
    private final boolean isFirstCoordEdge;

    /* Immutable references, but inner state is mutable */
    private final boolean[] usedIDs;
    /* Immutable references, but inner state of each Node might change. Indexed by tree height. Not to be synchronized. */
    private final SolutionNode[] solution;

    /* Mutable */
    private TreeNode node;
    private long sets = 0, expands = 0, undos = 0;

    protected TreeSolver(int dimensionX, int dimensionY, int dimensionZ, EnumMap<CubeType, ICube[]> cubeMap, Coordinate[] coords) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.usedIDs = new boolean[dimensionX * dimensionY * dimensionZ + 1];
        this.solution = new SolutionNode[dimensionX * dimensionY * dimensionZ];

        this.isFirstCoordEdge = this.initSolution(coords, cubeMap);
    }

    protected void syncStartingNode(TreeSolver original, int number) {
        // Makes sure each solver starts on a different branch
        this.node = original.node.children[number % original.node.children.length];
        this.node.status = TreeNode.Status.BeingPopulated;
        this.solution[this.node.getHeight()].set(this.node.getCube());
        this.sets++;
    }

    private boolean initSolution(Coordinate[] coords, EnumMap<CubeType, ICube[]> cubeMap) {
        SolutionNode[][][] tmp = new SolutionNode[dimensionX][dimensionY][dimensionZ];

        boolean isFirstCoordEdge = false;
        for (int i = 0; i < coords.length; i++) {
            Coordinate c = coords[i];
            ICubeFilter f = CubeFilterFactory.defaultFilter();
            if(c.x() == 0) f.setSide(ICube.Side.Back, Triangle.None);
            if(c.x() == dimensionX - 1) f.setSide(ICube.Side.Front, Triangle.None);
            if(c.y() == 0) f.setSide(ICube.Side.Left, Triangle.None);
            if(c.y() == dimensionY - 1) f.setSide(ICube.Side.Right, Triangle.None);
            if(c.z() == 0) f.setSide(ICube.Side.Down, Triangle.None);
            if(c.z() == dimensionZ - 1) f.setSide(ICube.Side.Up, Triangle.None);

            CubeType type = CubeType.get(f.getTriangles());
            ArrayCubeSorter s = CubeSorterFactory.from(cubeMap.get(type), type);
            if(i == 0 && type == CubeType.ThreeEdge) isFirstCoordEdge = true;

            this.solution[i] = new SolutionNode(c, f, s);
            tmp[c.x()][c.y()][c.z()] = this.solution[i];
        }

        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    for (ICube.Side s : ICube.Side.values()) {
                        if(x + s.x >= 0 && x + s.x < dimensionX &&
                                y + s.y >= 0 && y + s.y < dimensionY &&
                                z + s.z >= 0 && z + s.z < dimensionZ) {
                            tmp[x][y][z].neighbors[s.ordinal()] = tmp[x + s.x][y + s.y][z + s.z];
                        }
                    }
                }
            }
        }

        return isFirstCoordEdge;
    }

    @Override
    public void prepare() {
        this.node = new TreeNode();
        if(isFirstCoordEdge && (dimensionX == dimensionY || dimensionY == dimensionZ || dimensionX == dimensionZ)) {
            ICube[] cubes = this.solution[0].sorter.matchingAny(this.solution[0].filter);
            this.node.populate(cubes);
        }else {
            expandCurrentNode();
        }
    }

    @Override
    public IPuzzleSolution solve() throws PuzzleNotSolvableException {
        return solveConcurrent();
    }

    @Override
    public IPuzzleSolution solveConcurrent() throws PuzzleNotSolvableException {
        System.out.println("Starting node: " + this.node);

        int maxHeight = this.solution.length - 1;
        do {
            if(Thread.interrupted()) {
                if(Puzzle.LOG) System.out.println("Got interrupted, exiting!");
                return null;
            }
            expandCurrentNode();
            setNextNode();
        } while(this.node.getHeight() < maxHeight);

        return this;
    }

    public void expandCurrentNode() {
        if(this.node.isBeingPopulated()) {
            this.node.populate(this.solution[this.node.getHeight() + 1].matching());
            expands++;
        }
    }

    /**
     * Traverses the tree down one level.
     */
    public void setNextNode() throws PuzzleNotSolvableException {
        TreeNode nextNode = this.node.getNext();
        if(nextNode == null) { // Either this node is empty, or all children are already being processed by another thread
//            TreeNode nextSibling = this.node.parent.getNext();
//            if(nextSibling != null) { // The parent of the current node has a "free" child
//                this.node.validate(); // Current node may be dead at this point
//                this.undos++;
//                if(Puzzle.DEBUG && nextSibling.isDead()) throw new ConcurrentModificationException();
//                this.node = nextSibling;
//                this.solution[this.node.getHeight()].set(this.node.getCube());
//                this.sets++;
//            }else {
                this.undo();
                this.setNextNode();
//            }
        }
        else {
            if(Puzzle.DEBUG && nextNode.isDead()) throw new ConcurrentModificationException();
            this.node = nextNode;
            this.solution[this.node.getHeight()].set(this.node.getCube());
            this.sets++;
        }
    }

    private void undo() {
        this.solution[this.node.getHeight()].unset(); // Will crash if on root node (root.getHeight() = -1)
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
        Coordinate c = this.solution[h].coordinate;
        return String.format("[%d,%d,%d] Height %d/%d with [%d set, %d expand, %d undo] per second",
                c.x(), c.y(), c.z(), h, m, diff_sets, diff_expands, diff_undos);
    }

    @Override
    public boolean canRunConcurrent() {
        return true;
    }

    @Override
    public String serialize() {
        StringBuilder b = new StringBuilder();
        b.append("Dimension ");
        b.append(this.getDimensionX());
        b.append(',');
        b.append(this.getDimensionY());
        b.append(',');
        b.append(this.getDimensionZ());
        b.append('\n');

        for (SolutionNode n : this.solution) {
            b.append('[');
            b.append(n.coordinate.x() + 1);
            b.append(',');
            b.append(n.coordinate.y() + 1);
            b.append(',');
            b.append(n.coordinate.z() + 1);
            b.append("] ");
            ICube c = n.cube;
            if(c != null) b.append(c.serialize());
            else throw new IllegalStateException("Cube may not be null!");
            b.append('\n');
        }

        return b.toString();
    }

    /**
     * Each node represents one position in the solution
     */
    private class SolutionNode {
        private final Coordinate coordinate;
        private final ICubeFilter filter;
        private final ArrayCubeSorter sorter;
        /** Neighboring nodes, indexed by Side.ordinal() */
        private final SolutionNode[] neighbors = new SolutionNode[6];
        private ICube cube = null;

        private SolutionNode(Coordinate coordinate, ICubeFilter filter, ArrayCubeSorter sorter) {
            this.coordinate = coordinate;
            this.filter = filter;
            this.sorter = sorter;
        }

        /**
         * Returns all cubes that match into this node
         */
        private ICube[] matching() {
            return this.sorter.matching(this.filter, TreeSolver.this::isFree);
        }

        private void set(ICube cube) {
            assert this.filter.match(cube);

            if(this.cube != null) TreeSolver.this.usedIDs[this.cube.getIdentifier()] = false;
            TreeSolver.this.usedIDs[cube.getIdentifier()] = true;
            this.cube = cube;

            for (int i = 0; i < 6; i++) {
                SolutionNode neighbor = this.neighbors[i];
                if(neighbor != null) {
                    neighbor.filter.setSide(ICube.Side.getOpposite(i), cube.getMatchingTriangle(i));
                }
            }
        }

        private void unset() {
            if(this.cube != null) TreeSolver.this.usedIDs[this.cube.getIdentifier()] = false;
            this.cube = null;

            for (int i = 0; i < 6; i++) {
                SolutionNode neighbor = this.neighbors[i];
                if(neighbor != null) {
                    neighbor.filter.setSide(ICube.Side.getOpposite(i), (byte) 5);
                }
            }
        }
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
            assert this.children != null;

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
        public void populate(ICube[] cubes) {
            assert this.status == Status.BeingPopulated;

            if (cubes.length == 0) {
                setDead();
                return;
            }

            //synchronized (this) {
                this.children = new TreeNode[cubes.length];
                for (int i = 0; i < cubes.length; i++) {
                    this.children[i] = new TreeNode(this, cubes[i]);
                }
                this.status = Status.Populated;
            //}
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
            if (this.children == null || this.children.length == 0) return;
            for (TreeNode n : this.children) {
                if (!n.isDead()) return;
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

    @Override
    public IPuzzleSolver deepClone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICube set(int x, int y, int z, ICube cube) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICube getSolutionAt(int x, int y, int z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICubeFilter getFilterAt(int x, int y, int z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDimensionX() {
        return this.dimensionX;
    }

    @Override
    public int getDimensionY() {
        return this.dimensionY;
    }

    @Override
    public int getDimensionZ() {
        return this.dimensionZ;
    }
}