package abstractions;

import java.util.stream.Stream;

public record Coordinate(int x, int y, int z) {

    public <T> T arrayGet(T[][][] arr) {
        return arr[x][y][z];
    }

    /**
     * Generates a stream of coordinates, each starting at 0 and stopping at c < maxC
     */
    public static Stream<Coordinate> generate(int maxX, int maxY, int maxZ) {
        var b = Stream.<Coordinate>builder();

        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                for (int z = 0; z < maxZ; z++) {
                    b.add(new Coordinate(x, y, z));
                }
            }
        }

        return b.build();
    }
}
