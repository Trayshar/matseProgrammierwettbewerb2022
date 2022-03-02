package implementation.solver;

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
        for (int z = 0; z < dimensionZ; z++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int x = 0; x < dimensionX; x++) {
                    coords[i++] = new Coordinate(x, y, z);
                }
            }
        }

//        int x = 0, y = 0, z = 0;
//        for (int i = 0; i < coords.length; i++) {
//            if(validX(x + 1)) {
//                x++;
//            }else if(validY(y + 1)) {
//                x = 0;
//                y++;
//            }else if(validZ(z + 1)) {
//                x = 0;
//                y = 0;
//                z++;
//            }
//            coords[i] = new Coordinate(x, y, z);
//        }
        return coords;
    }
}
