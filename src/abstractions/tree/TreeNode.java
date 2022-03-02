package abstractions.tree;

import abstractions.PuzzleNotSolvableException;
import abstractions.cube.ICube;

/**
 * Each node has exactly one parent and n children, that are populated on demand later. Only one thread may ever populate the same node at the same time.
 */
public class TreeNode {
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
            synchronized (n) {
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
