package abstractions;

import abstractions.cube.ICube;

public interface IPuzzelSolution {
    /**
     * Serializes the solution into a String which is written into the returned file
     */
    String serialize();

    /**
     * Set the cube at the specific position
     * @throws IllegalStateException If the given cube does not fit
     * @return The cube that was at this position before, or null
     */
    ICube set(int x, int y, int z, ICube cube);
}
