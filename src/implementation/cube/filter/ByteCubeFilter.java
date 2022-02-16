package implementation.cube.filter;

import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;

import java.util.Arrays;

/**
 * {@link ICubeFilter} implementation that uses byte arrays for all its operations.
 * Use the match(bytes...) function for optimal performance.
 */
public class ByteCubeFilter implements ICubeFilter {

    private static final byte None = (byte) Triangle.None.ordinal();
    private static final byte BottomLeft = (byte) Triangle.BottomLeft.ordinal();
    private static final byte TopLeft = (byte) Triangle.TopLeft.ordinal();
    private static final byte TopRight = (byte) Triangle.TopRight.ordinal();
    private static final byte BottomRight = (byte) Triangle.BottomRight.ordinal();
    private static final byte Any = (byte) Triangle.Any.ordinal();
    private static final byte AnyNotNone = (byte) Triangle.AnyNotNone.ordinal();

    private final byte[] sides = new byte[6];

    public ByteCubeFilter(Triangle... sides) {
        for (int i = 0; i < 6; i++) {
            this.sides[i] = (byte) sides[i].ordinal();
        }
    }

    public ByteCubeFilter() {}

    public ByteCubeFilter(byte... sides) {
        System.arraycopy(sides, 0, this.sides, 0, 6);
    }

    @Override
    public boolean match(byte... triangles) {
        for (int i = 0; i < 6; i++) {
            if(this.sides[i] == AnyNotNone) {
                if(this.sides[i] == None) return false;
            }else if(this.sides[i] != Any) {
                if(this.sides[i] != triangles[i]) return false;
            }
        }

        return true;
    }

    @Override
    public void setSide(ICube.Side side, Triangle triangle) {
        this.sides[side.ordinal()] = (byte) triangle.ordinal();
    }

    @Override
    public Triangle getSide(ICube.Side side) {
        return Triangle.valueOf(this.sides[side.ordinal()]);
    }

    @Override
    public ByteCubeFilter clone() {
        // Can't use native clone here since "sides" is a final array, which is cloned by reference
        return new ByteCubeFilter(this.sides);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteCubeFilter that = (ByteCubeFilter) o;
        return Arrays.equals(sides, that.sides);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(sides);
    }

    @Override
    public String toString() {
        return "ByteCubeFilter{" +
                "sides=" + Arrays.toString(sides) +
                '}';
    }
}
