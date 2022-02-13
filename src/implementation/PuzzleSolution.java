package implementation;

import abstractions.IPuzzelSolution;
import abstractions.cube.ICube;

public class PuzzleSolution implements IPuzzelSolution {
    public final int dimensionX, dimensionY, dimensionZ;
    public final ICube[][][] cubes;

    public PuzzleSolution(int dimensionX, int dimensionY, int dimensionZ) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.cubes = new ICube[dimensionX][dimensionY][dimensionZ];
    }

    @Override
    public String serialize() {
        StringBuilder b = new StringBuilder();
        b.append("Dimension ");
        b.append(dimensionX);
        b.append('c');
        b.append(dimensionY);
        b.append('c');
        b.append(dimensionZ);
        b.append('c');
        b.append('\n');

        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    b.append('[');
                    b.append(x + 1);
                    b.append(',');
                    b.append(y + 1);
                    b.append(',');
                    b.append(z + 1);
                    b.append(',');
                    b.append("] ");
                    b.append(cubes[x][y][z].serialize());
                    b.append('\n');
                }
            }
        }

        return b.toString();
    }
}
