package implementation.cube.filter;

import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;

/**
 * {@link ICubeFilter} implementation that uses triangle arrays for all its operations.
 * Use the match(Triangle...) function for optimal performance.
 */
@Deprecated
public class TriCubeFilter implements ICubeFilter {

    private final Triangle[] sides = new Triangle[6];

    public TriCubeFilter(Triangle... sides) {
        assert sides.length == 6;

        System.arraycopy(sides, 0, this.sides, 0, 6);
    }

    @Override
    public boolean match(Triangle... cube) {
        for (int i = 0; i < 6; i++) {
            if(this.sides[i] == Triangle.AnyNotNone) {
                if(this.sides[i] == Triangle.None) return false;
            }
            if(this.sides[i] != Triangle.Any) {
                if(this.sides[i] != cube[i]) return false;
            }
        }

        return true;
    }

    @Override
    public boolean match(byte... triangles) {
        return this.match(
                Triangle.valueOf(triangles[0]),
                Triangle.valueOf(triangles[1]),
                Triangle.valueOf(triangles[2]),
                Triangle.valueOf(triangles[3]),
                Triangle.valueOf(triangles[4]),
                Triangle.valueOf(triangles[5])
        );
    }

    @Override
    public void setSide(ICube.Side side, Triangle triangle) {
        this.sides[side.ordinal()] = triangle;
    }

    @Override
    public Triangle getSide(ICube.Side side) {
        return this.sides[side.ordinal()];
    }

    @Override
    public TriCubeFilter clone() {
        // Can't use native clone here since "sides" is a final array, which is cloned by reference
        return new TriCubeFilter(this.sides);
    }
}
