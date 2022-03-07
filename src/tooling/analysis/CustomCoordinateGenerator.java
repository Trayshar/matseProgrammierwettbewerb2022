package tooling.analysis;

import abstractions.Coordinate;
import abstractions.CoordinateGenerator;

public class CustomCoordinateGenerator extends CoordinateGenerator {

    public CustomCoordinateGenerator(int dimensionX, int dimensionY, int dimensionZ) {
        super(dimensionX, dimensionY, dimensionZ);
    }

    @Override
    public Coordinate[] generate() {
        Coordinate[] coords = new Coordinate[dimensionX * dimensionY * dimensionZ];
        int maxX = dimensionX - 1;
        int maxY = dimensionY - 1;
        int maxZ = dimensionZ - 1;
        int diffXY = dimensionX - dimensionY; // We know that X ≥ Y
        int diffZY = dimensionY - dimensionZ; // We know that Y ≥ Z

        int i = 0;

        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    coords[i++] = new Coordinate(x, y, z);
                }
            }
        }

        // First two y in zig-zag pattern
//        for (int x = 0; x < dimensionX; x++) {
//            for (int z = 0; z < dimensionZ; z++) {
//                coords[i++] = new Coordinate(x, 0, z);
//                coords[i++] = new Coordinate(x, 1, z);
//            }
//        }
//
//        for (int x = 0; x < dimensionX; x++) {
//            for (int y = 2; y < dimensionY; y++) {
//                for (int z = 0; z < dimensionZ; z++) {
//                    coords[i++] = new Coordinate(x, y, z);
//                }
//            }
//        }

        // Circular expansion, then normal
//        for (int x = 0; x < dimensionX - diffXY; x++) {
//            for (int y = 0; y < x + 1; y++) {
//                for (int z = 0; z < dimensionZ; z++) {
//                    coords[i++] = new Coordinate(x, y, z);
//                }
//            }
//            for (int x2 = 0; x2 < x; x2++) {
//                for (int z = 0; z < dimensionZ; z++) {
//                    coords[i++] = new Coordinate(x2, x, z);
//                }
//            }
//        }
//
//        for (int x = dimensionX - diffXY; x < dimensionX; x++) {
//            for (int y = 0; y < dimensionY; y++) {
//                for (int z = 0; z < dimensionZ; z++) {
//                    coords[i++] = new Coordinate(x, y, z);
//                }
//            }
//        }
        return coords;
    }
}
