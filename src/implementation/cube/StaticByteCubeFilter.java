package implementation.cube;

import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;

/**
 * {@link ICubeFilter} implementation that uses byte arrays for all its operations. Use the match(bytes...) function.
 */
public class StaticByteCubeFilter implements ICubeFilter {

    private static final byte None = (byte) Triangle.None.ordinal();
    private static final byte BottomLeft = (byte) Triangle.BottomLeft.ordinal();
    private static final byte TopLeft = (byte) Triangle.TopLeft.ordinal();
    private static final byte TopRight = (byte) Triangle.TopRight.ordinal();
    private static final byte BottomRight = (byte) Triangle.BottomRight.ordinal();
    private static final byte ANY = (byte) Triangle.Any.ordinal();

    private final byte[] sides = new byte[6];

    public StaticByteCubeFilter(Triangle... sides) {
        for (int i = 0; i < 6; i++) {
            this.sides[i] = (byte) sides[i].ordinal();
        }
    }

    @Override
    public boolean match(byte... triangles) {
        for (int i = 0; i < 6; i++) {
            if(this.sides[i] != ANY) {
                if(this.sides[i] != triangles[i]) return false;
            }
        }

        return true;
    }
}
