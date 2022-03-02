package abstractions;

import abstractions.cube.ICube;

public record Coordinate(int x, int y, int z) {

    public <T> T arrayGet(T[][][] arr) {
        return arr[x][y][z];
    }

    /**
     * Returns the side this coordinate touches the other coordinate on, or null if they don't touch.
     */
    public ICube.Side adjacentTo(Coordinate other) {
        return ICube.Side.getByDirection(x - other.x, y - other.y, z - other.z);
    }
}
