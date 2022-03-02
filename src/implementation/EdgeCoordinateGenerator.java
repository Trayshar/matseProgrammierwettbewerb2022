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

        // Lowest plane, edges only
        for (int x = 0; x < maxX; x++) {
            coords[i++] = new Coordinate(x, 0, 0);
        }
        for (int y = 0; y < maxY; y++) {
            coords[i++] = new Coordinate(maxX, y, 0);
        }
        for (int x = maxX; x > 0; x--) {
            coords[i++] = new Coordinate(x, maxY, 0);
        }
        for (int y = maxY; y > 0; y--) {
            coords[i++] = new Coordinate(0, y, 0);
        }

        // "Pillars" between z=0 and z=maxZ
        for (int z = 1; z < maxZ; z++) {
            coords[i++] = new Coordinate(0, 0, z);
            coords[i++] = new Coordinate(maxX, 0, z);
            coords[i++] = new Coordinate(maxX, maxY, z);
            coords[i++] = new Coordinate(0, maxY, z);
        }

        // Top plane, edges only
        for (int x = 0; x < maxX; x++) {
            coords[i++] = new Coordinate(x, 0, maxZ);
        }
        for (int y = 0; y < maxY; y++) {
            coords[i++] = new Coordinate(maxX, y, maxZ);
        }
        for (int x = maxX; x > 0; x--) {
            coords[i++] = new Coordinate(x, maxY, maxZ);
        }
        for (int y = maxY; y > 0; y--) {
            coords[i++] = new Coordinate(0, y, maxZ);
        }

        // low plane, filling
        for (int x = 1; x < maxX; x++) {
            for (int y = 1; y < maxY; y++) {
                coords[i++] = new Coordinate(x, y, 0);
            }
        }

        // top plane, filling
        for (int x = 1; x < maxX; x++) {
            for (int y = 1; y < maxY; y++) {
                coords[i++] = new Coordinate(x, y, maxZ);
            }
        }

        // left plane, filling
        for (int x = 1; x < maxX; x++) {
            for (int z = 1; z < maxZ; z++) {
                coords[i++] = new Coordinate(x, 0, z);
            }
        }

        // right plane, filling
        for (int x = 1; x < maxX; x++) {
            for (int z = 1; z < maxZ; z++) {
                coords[i++] = new Coordinate(x, maxY, z);
            }
        }

        // front plane, filling
        for (int z = 1; z < maxZ; z++) {
            for (int y = 1; y < maxY; y++) {
                coords[i++] = new Coordinate(0, y, z);
            }
        }

        // back plane, filling
        for (int z = 1; z < maxZ; z++) {
            for (int y = 1; y < maxY; y++) {
                coords[i++] = new Coordinate(maxX, y, z);
            }
        }

        // Filling the "inner cube"
        for (int x = 1; x < maxX; x++) {
            for (int y = 1; y < maxY; y++) {
                for (int z = 1; z < maxZ; z++) {
                    coords[i++] = new Coordinate(x, y, z);
                }
            }
        }

        assert i == coords.length;

        return coords;
    }
}
