package abstractions;

import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;

public interface IPuzzleSolution {
    /**
     * Serializes the solution into a String which is written into the returned file
     */
    default String serialize() {
        StringBuilder b = new StringBuilder();
        b.append("Dimension ");
        b.append(this.getDimensionX());
        b.append(',');
        b.append(this.getDimensionY());
        b.append(',');
        b.append(this.getDimensionZ());
        b.append('\n');

        for (int x = 0; x < this.getDimensionX(); x++) {
            for (int y = 0; y < this.getDimensionY(); y++) {
                for (int z = 0; z < this.getDimensionZ(); z++) {
                    b.append('[');
                    b.append(x + 1);
                    b.append(',');
                    b.append(y + 1);
                    b.append(',');
                    b.append(z + 1);
                    b.append("] ");
                    ICube c = this.getSolutionAt(x, y, z);
                    if(c != null) b.append(c.serialize());
                    else b.append("null");
                    b.append('\n');
                }
            }
        }

        return b.toString();
    }

    /**
     * Set the cube at the specific position
     * @throws IllegalStateException If the given cube does not fit
     * @return The cube that was at this position before, or null
     */
    ICube set(int x, int y, int z, ICube cube);

    /**
     * Returns the solution at the given point; May return null.
     */
    ICube getSolutionAt(int x, int y, int z);

    /**
     * Returns the requirements to the cube at the given position;
     * No guarantees are made for the returned value if a solution for the given position already exists.
     */
    ICubeFilter getFilterAt(int x, int y, int z);

    /**
     * Set the cube at the specific position
     * @throws IllegalStateException If the given cube does not fit
     * @return The cube that was at this position before, or null
     */
    default ICube set(Coordinate c, ICube cube) {
        return this.set(c.x(), c.y(), c.z(), cube);
    }

    /**
     * Returns the solution at the given point; May return null.
     */
    default ICube getSolutionAt(Coordinate c) {
        return this.getSolutionAt(c.x(), c.y(), c.z());
    }

    /**
     * Returns the requirements to the cube at the given position;
     * No guarantees are made for the returned value if a solution for the given position already exists.
     */
    default ICubeFilter getFilterAt(Coordinate c) {
        return this.getFilterAt(c.x(), c.y(), c.z());
    }

    int getDimensionX();

    int getDimensionY();

    int getDimensionZ();
}
