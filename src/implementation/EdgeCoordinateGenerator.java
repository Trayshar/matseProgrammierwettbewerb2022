package implementation;

import abstractions.Coordinate;
import abstractions.CoordinateGenerator;

public class EdgeCoordinateGenerator extends CoordinateGenerator {

    public EdgeCoordinateGenerator(int dimensionX, int dimensionY, int dimensionZ) {
        super(dimensionX, dimensionY, dimensionZ);
    }

    @Override
    public Coordinate[] generate() {
        Coordinate[] coords = new Coordinate[dimensionX * dimensionY * dimensionZ];
        int maxX = dimensionX - 1;
        int maxY = dimensionY - 1;
        int maxZ = dimensionZ - 1;

        int i = 0;
        for (int y = 0; y < dimensionY; y++) {
            for (int z = 0; z < dimensionZ; z++) {
                coords[i++] = new Coordinate(0, y, z);
            }
        }

        for (int x = 1; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    coords[i++] = new Coordinate(x, y, z);
                }
            }
        }
        return coords;
    }
}
