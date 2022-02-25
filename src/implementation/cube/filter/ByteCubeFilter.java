package implementation.cube.filter;

import abstractions.Orientation;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;
import implementation.Puzzle;

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
    private static final byte AnyNotNone = (byte) Triangle.AnyNotNone.ordinal();

    private final byte[] sides = new byte[6];

    public ByteCubeFilter(Triangle... sides) {
        for (int i = 0; i < 6; i++) {
            this.sides[i] = (byte) sides[i].ordinal();
        }
    }

    public ByteCubeFilter(ByteCubeFilter raw, Orientation o) {
        for (int j = 0; j < 6; j++) {
            if(raw.sides[j] != Triangle.None.ordinal()) { // Only write if there is a triangle
                byte tmp  = (byte) (o.triangleOffset[j] + raw.sides[j]);
                if (tmp > 4) tmp -= 4;
                sides[o.side[j]] = tmp;
                if(Puzzle.DEBUG && sides[o.side[j]] == 0) {
                    sides[o.side[j]] = 1;
                    System.out.printf("[] Illegal rotation operation (%d, %d)\n", o.triangleOffset[j], raw.sides[j]);
                }
            }
        }
    }

    public ByteCubeFilter(byte... sides) {
        System.arraycopy(sides, 0, this.sides, 0, 6);
    }

    @Override
    public boolean match(byte... triangles) {
        for (int i = 0; i < 6; i++) {
            if(this.sides[i] == AnyNotNone) {
                if(triangles[i] == None) return false;
            }else {
                if(triangles[i] != this.sides[i]) return false;
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
    public int getNumTriangle() {
        int c = 0;

        for (byte side : sides) {
            if(side != None) c++;
        }

        return c;
    }

    @Override
    public ICubeFilter cloneFilter() {
        return new ByteCubeFilter(this.sides);
    }

    @Override
    public int getUniqueId() {
        return  sides[0] * 7776 +
                sides[1] * 1296 +
                sides[2] * 216 +
                sides[3] * 36 +
                sides[4] * 6 +
                sides[5];
    }

    @Override
    protected ByteCubeFilter clone() {
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
        return this.getUniqueId();
    }

    @Override
    public String toString() {
        return "ByteCubeFilter{" +
                "sides=" + Arrays.toString(sides) +
                '}';
    }
}
