package implementation.solution;

import abstractions.Coordinate;
import abstractions.ISolutionMold;
import abstractions.cube.CubeType;
import implementation.cube.CubeSorter;

public class MoldFactory {

    private static final ISolutionMold[][][] premolded = new ISolutionMold[3][3][3];
    static {
        premolded[0][0][0] = new ISolutionMold() {
            @Override
            public Coordinate getPosition(int stage) {
                return new Coordinate(0, 0, 0);
            }

            @Override
            public boolean isSolvable(CubeSorter sorter) {
                return sorter.unique(CubeType.Zero) == 1;
            }
        };
    }

    public static ISolutionMold makeMold(int dimX, int dimY, int dimZ) {
        //if(dimX < 4 && dimY < 4 && dimZ < 4) return premolded[dimX-1][dimY-1][dimZ-1];

        if(dimZ == 1) {
            if(dimY == 1) {
                if(dimX == 1) {
                    return premolded[0][0][0];
                }
            }
        }
        return null;
    }

    /**
     * Checks whether a cube solution with the given dimensions is possible with the given cubes
     * X ≥ Y ≥ Z
     */
    public static boolean isSolvable(int dimX, int dimY, int dimZ, CubeSorter sorter) {
        if(dimX * dimY * dimZ != sorter.getNumCubes()) return false;

        if(dimZ == 1) {
            if(dimY == 1) {
                if(dimX == 1) { // x = y = z = 1
                    // + -- +
                    // | 0  |
                    // + -- +
                    return sorter.unique(CubeType.Zero) == 1;
                }else{ // y = z = 1, x > 1
                    // + -- + -- + ... + -- + -- +
                    // | 1  | 2o | ... | 2o | 1  |
                    // + -- + -- + ... + -- + -- +
                    return sorter.unique(CubeType.One) == 2 && sorter.unique(CubeType.TwoOpposite) == dimX - 2;
                }
            } else if(dimY == 2) { // x ≥ 2, y = 2, z = 1
                // + -- + -- + ... + -- + -- +
                // | 2c | 3c | ... | 3c | 2c |
                // + -- + -- + ... + -- + -- +
                // | 2c | 3c | ... | 3c | 2c |
                // + -- + -- + ... + -- + -- +
                return sorter.unique(CubeType.TwoConnected) == 4 && sorter.unique(CubeType.ThreeConnected) == dimX - 4;
            } else { // x, y ≥ 3, z = 1
                // + -- + -- + ... + -- + -- +
                // | 2c | 3c | ... | 3c | 2c |
                // + -- + -- + ... + -- + -- +
                // | 3c | 4r | ... | 4r | 3c |
                // + -- + -- + ... + -- + -- +
                // .    .    .     .    .    .
                // .    .    .     .    .    .
                // + -- + -- + ... + -- + -- +
                // | 3c | 4r | ... | 4r | 3c |
                // + -- + -- + ... + -- + -- +
                // | 2c | 3c | ... | 3c | 2c |
                // + -- + -- + ... + -- + -- +
                return sorter.unique(CubeType.TwoConnected) == 4 &&
                       sorter.unique(CubeType.ThreeConnected) == 2*dimX + 2*dimY - 8 &&
                       sorter.unique(CubeType.FourRound) == (dimX - 2) * (dimY - 2);
            }
//        } else if(dimZ == 2) {
//            if(dimY == 2) { // x ≥ 2, y = 2, z = 2
//                // + -- + -- + ... + -- + -- +  + -- + -- + ... + -- + -- +
//                // | 3e | 4c | ... | 4c | 3e |  | 3e | 4c | ... | 4c | 3e |
//                // + -- + -- + ... + -- + -- +  + -- + -- + ... + -- + -- +
//                // | 3e | 4c | ... | 4c | 3e |  | 3e | 4c | ... | 4c | 3e |
//                // + -- + -- + ... + -- + -- +  + -- + -- + ... + -- + -- +
//                return sorter.unique(CubeType.ThreeEdge) == 8 && sorter.unique(CubeType.FourConnected) == 4*dimX - 8;
//            } else { // x, y ≥ 3, z = 2
//                // + -- + -- + ... + -- + -- + Other plane is identical...
//                // | 3e | 4c | ... | 4c | 3e |
//                // + -- + -- + ... + -- + -- +
//                // | 4c | 5  | ... | 5  | 4c |
//                // + -- + -- + ... + -- + -- +
//                // .    .    .     .    .    .
//                // .    .    .     .    .    .
//                // + -- + -- + ... + -- + -- +
//                // | 4c | 5  | ... | 5  | 4c |
//                // + -- + -- + ... + -- + -- +
//                // | 3e | 4c | ... | 4c | 3e |
//                // + -- + -- + ... + -- + -- +
//                return sorter.unique(CubeType.ThreeEdge) == 8 &&
//                       sorter.unique(CubeType.FourConnected) == 4 * (dimX + dimY - 4) &&
//                       sorter.unique(CubeType.Five) == 2 * (dimX - 2) * (dimY - 2);
//            }
        } else {// x, y, z ≥ 3
            // + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +
            // | 3e | 4c | ... | 4c | 3e |    | 4c | 5  | ... | 5  | 4c |    | 3e | 4c | ... | 4c | 3e |
            // + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +
            // | 4c | 5  | ... | 5  | 4c |    | 5  | 6  | ... | 6  | 5  |    | 4c | 5  | ... | 5  | 4c |
            // + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +
            // .    .    .     .    .    .    .    .    .     .    .    .    .    .    .     .    .    .
            // .    .    .     .    .    .    .    .    .     .    .    .    .    .    .     .    .    .
            // + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +
            // | 4c | 5  | ... | 5  | 4c |    | 5  | 6  | ... | 6  | 5  |    | 4c | 5  | ... | 5  | 4c |
            // + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +
            // | 3e | 4c | ... | 4c | 3e |    | 4c | 5  | ... | 5  | 4c |    | 3e | 4c | ... | 4c | 3e |
            // + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +
            return sorter.unique(CubeType.ThreeEdge) == 8 &&
                   sorter.unique(CubeType.FourConnected) == 4 * (dimX + dimY + dimZ - 6) && // 4*(dimX-2)+4*(dimY-2)+4*(dimZ-2)
                                                // 2 * (dimX - 2) * (dimY - 2) + 2 * (dimZ - 2) * (dimX - 2) + 2 * (dimZ - 2) * (dimY - 2)
                   sorter.unique(CubeType.Five) == 2 * ( (dimX - 2) * (dimY - 2) + (dimZ - 2) * (dimX - 2) + (dimZ - 2) * (dimY - 2) ) &&
                   sorter.unique(CubeType.Six) == (dimX - 2) * (dimY - 2) * (dimZ - 2);
        }
    }
}
