package implementation.solution;

import abstractions.IPuzzleSolution;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;

public record NoSolution(int dimX, int dimY, int dimZ) implements IPuzzleSolution {
    @Override
    public String serialize() {
        return "Dimension " +
                this.getDimensionX() +
                ',' +
                this.getDimensionY() +
                ',' +
                this.getDimensionZ() +
                '\n';
    }

    @Override
    public ICube set(int x, int y, int z, ICube cube) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICube getSolutionAt(int x, int y, int z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICubeFilter getFilterAt(int x, int y, int z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDimensionX() {
        return dimX;
    }

    @Override
    public int getDimensionY() {
        return dimY;
    }

    @Override
    public int getDimensionZ() {
        return dimZ;
    }
}
