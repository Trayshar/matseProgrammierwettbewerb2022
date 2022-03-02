package abstractions;

public record Coordinate(int x, int y, int z) {

    public <T> T arrayGet(T[][][] arr) {
        return arr[x][y][z];
    }
}
