package tooling.analysis;

import abstractions.Coordinate;
import abstractions.CoordinateGenerator;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import abstractions.cube.Triangle;
import implementation.cube.sorter.ArrayCubeSorter;
import implementation.cube.sorter.CubeSorterFactory;
import implementation.solution.DynamicPuzzleSolution;

import java.io.Serializable;
import java.util.Arrays;
import java.util.ConcurrentModificationException;

public class TreeBuilder  {
    /* Immutable dimensions of this solver */
    public final int dimensionX, dimensionY, dimensionZ;
    /* Immutable, indexed by tree height */
    private final Coordinate[] coords;
    private final TreeNode root;
    private final CubeType[] types;

    /* Immutable, only change are queries getting cached. Indexed by tree height. */
    private final ArrayCubeSorter[] sorter;

    /* Immutable references, but inner state is mutable */
    private final DynamicPuzzleSolution solution;
    private final boolean[] usedIDs;

    /* Mutable */
    private TreeNode node;
    private long sets = 0, expands = 0, undos = 0;

    protected TreeBuilder(int dimensionX, int dimensionY, int dimensionZ, ICube[] threeEdgeC, ICube[] fourConnectedC, ICube[] fiveC, ICube[] sixC, CoordinateGenerator generator) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.solution = new DynamicPuzzleSolution(dimensionX, dimensionY, dimensionZ);
        this.coords = generator.generate();
        this.sorter = new ArrayCubeSorter[dimensionX * dimensionY * dimensionZ];
        this.usedIDs = new boolean[dimensionX * dimensionY * dimensionZ + 1];
        this.root = new TreeNode();
        this.node = this.root;
        this.types = new CubeType[dimensionX * dimensionY * dimensionZ];

        ArrayCubeSorter threeEdge = CubeSorterFactory.from(threeEdgeC, CubeType.ThreeEdge);
        ArrayCubeSorter fourConnected = CubeSorterFactory.from(fourConnectedC, CubeType.FourConnected);
        ArrayCubeSorter five = CubeSorterFactory.from(fiveC, CubeType.Five);
        ArrayCubeSorter six = CubeSorterFactory.from(sixC, CubeType.Six);

        for (int i = 0; i < this.coords.length; i++) {
            var t = CubeType.get(this.solution.getFilterAt(this.coords[i]).getTriangles());
            this.types[i] = t;
            switch (t) {
                case ThreeEdge -> this.sorter[i] = threeEdge;
                case FourConnected -> this.sorter[i] = fourConnected;
                case Five -> this.sorter[i] = five;
                case Six -> this.sorter[i] = six;
            }
        }
    }

    public TreeNode build() {
        System.out.println("Starting!");
        if(dimensionX == dimensionY || dimensionY == dimensionZ || dimensionX == dimensionZ) {
            ICube[] cubes = this.sorter[0].matchingAny(this.solution.getFilterAt(this.coords[0]));
            System.out.println(Arrays.toString(cubes));

            if(1 < this.coords.length && cubes.length > 3) { // Lookahead to sort child notes
                ICube.Side side = this.coords[1].adjacentTo(this.coords[0]); // Side of "childHeight" that looks at "childHeight + 1"
                if(side != null) {
                    var sort = this.sorter[1];
                    var filter = this.solution.getFilterAt(this.coords[1]).cloneFilter();
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
        }

        int maxHeight = this.coords.length - 1;
        try{
            do {
                expandCurrentNode();
                setNextNode();
                if(this.node.getHeight() == maxHeight) {
                    this.node.setDead();
                    System.out.println("Found solution:");

                    TreeNode j = this.node;
                    while (j.parent != null) {
                        for (int i = 0; i < j.parent.children.length; i++) {
                            if(j.parent.children[i] == j) {
                                TreeAnalysis.upperLimit[j.getHeight()] = Math.max(TreeAnalysis.upperLimit[j.getHeight()], j.parent.children.length);
                                TreeAnalysis.lowerLimit[j.getHeight()] = Math.min(TreeAnalysis.lowerLimit[j.getHeight()], j.parent.children.length);
                                System.out.printf("Height %d, %d/%d, %s\n", j.getHeight(), i+1, j.parent.children.length, types[j.getHeight()]);
                                break;
                            }
                        }
                        j = j.parent;
                    }

                    //System.out.println(solution.serialize());
                    undo();
                }
            } while(true);
        }catch (IndexOutOfBoundsException | PuzzleNotSolvableException e) {
            e.printStackTrace();
        }

        return this.root;
    }

    public void expandCurrentNode() {
        if(this.node.isBeingPopulated()) {
            int childHeight = this.node.getHeight() + 1;
            ICube[] cubes = this.sorter[childHeight].matching(this.solution.getFilterAt(this.coords[childHeight]), this::isFree);

            if(childHeight + 1 < this.coords.length && cubes.length > 3) { // Lookahead to sort child notes
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
            if(nextNode.isDead()) throw new ConcurrentModificationException();
            this.node = nextNode;
            ICube tmp = this.solution.set(this.coords[this.node.getHeight()], this.node.getCube());
            if(tmp != null) throw new IllegalStateException();
            this.usedIDs[this.node.getCube().getIdentifier()] = true;
            this.sets++;
        }
    }

    private void undo() throws PuzzleNotSolvableException {
        // Undoes the operation in the solution object and freeing the id of the used cube
        int id = this.solution.undo();
        if(id == -1) throw new PuzzleNotSolvableException();
        if (id > 0 && !this.usedIDs[id]) {
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

    /**
     * Each node has exactly one parent and n children, that are populated on demand later. Only one thread may ever populate the same node at the same time.
     */
    protected static class TreeNode implements Serializable {
        protected final int height;
        protected final ICube cube;
        protected final TreeNode parent;
        protected TreeNode[] children = null;
        protected Status status;

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
            assert this.status != Status.Empty;

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
                this.children = new TreeNode[0];
                this.status = Status.Dead;
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
            if (this.children == null || this.isDead()) return;
            for (TreeNode n : this.children) {
                if (!n.isDead()) return;
            }
            this.setDead();
        }

        @Override
        public String toString() {
            return "TreeNode{" +
                    "height=" + height +
                    ", cube=" + cube.serialize() +
                    ", parent=" + parent.hashCode() +
                    ", children=" + children.length +
                    ", status=" + status +
                    '}';
        }

        protected enum Status implements Serializable{
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
            Dead(false),

            Winner(false);

            public final boolean eligible;

            Status(boolean eligible) {
                this.eligible = eligible;
            }
        }
    }
}