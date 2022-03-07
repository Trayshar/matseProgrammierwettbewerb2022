package implementation;

import abstractions.Coordinate;
import abstractions.CoordinateGenerator;

/**
 * Traverses an array linearly
 */
public class LinearCoordinateGenerator extends CoordinateGenerator {

    public LinearCoordinateGenerator(int dimensionX, int dimensionY, int dimensionZ) {
        super(dimensionX, dimensionY, dimensionZ);
    }

    @Override
    public Coordinate[] generate() {
        Coordinate[] coords = new Coordinate[dimensionX * dimensionY * dimensionZ];
        int i = 0;
        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    coords[i++] = new Coordinate(x, y, z);
                }
            }
        }
        return coords;
    }
}
