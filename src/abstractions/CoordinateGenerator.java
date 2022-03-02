package abstractions;

/**
 * Generator template
 */
public abstract class CoordinateGenerator {
    public final int dimensionX, dimensionY, dimensionZ;

    protected CoordinateGenerator(int dimensionX, int dimensionY, int dimensionZ) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
    }

    /**
     * Does the actual generation of coordinates
     */
    public abstract Coordinate[] generate();

    protected boolean validX(int val) {
        return val >= 0 && val < this.dimensionX;
    }

    protected boolean validY(int val) {
        return val >= 0 && val < this.dimensionY;
    }

    protected boolean validZ(int val) {
        return val >= 0 && val < this.dimensionZ;
    }
}
