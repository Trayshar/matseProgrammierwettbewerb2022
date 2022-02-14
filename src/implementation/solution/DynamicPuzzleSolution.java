package implementation.solution;

import abstractions.IPuzzleSolution;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;
import implementation.cube.filter.CubeFilterFactory;

import java.util.ArrayDeque;

public class DynamicPuzzleSolution implements IPuzzleSolution {
    public final int dimensionX, dimensionY, dimensionZ;
    private final ICube[][][] cubes;
    private final ICubeFilter[][][] filters;
    private final ArrayDeque<SetOperation> operations = new ArrayDeque<>();

    public DynamicPuzzleSolution(int dimensionX, int dimensionY, int dimensionZ) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.cubes = new ICube[dimensionX][dimensionY][dimensionZ];
        this.filters = new ICubeFilter[dimensionX][dimensionY][dimensionZ];

        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    ICubeFilter f = CubeFilterFactory.from(new byte[]{6, 6, 6, 6, 6, 6});
                    if(x == 0) f.setSide(ICube.Side.Left, Triangle.None);
                    if(x == dimensionX - 1) f.setSide(ICube.Side.Right, Triangle.None);
                    if(y == 0) f.setSide(ICube.Side.Front, Triangle.None);
                    if(y == dimensionY - 1) f.setSide(ICube.Side.Back, Triangle.None);
                    if(z == 0) f.setSide(ICube.Side.Down, Triangle.None);
                    if(z == dimensionZ - 1) f.setSide(ICube.Side.Up, Triangle.None);

                    this.filters[x][y][z] = f;
                }
            }
        }
    }

    @Override
    public ICube set(int x, int y, int z, ICube cube) {
        if(!this.filters[x][y][z].match(cube)) {
            throw new IllegalArgumentException("The given cube does not fit!");
        }
        for(ICube.Side s : ICube.Side.values()) {
            int x2 = x + s.x, y2 = y + s.y, z2 = z + s.z;
            if(valid(x2, dimensionX) && valid(y2, dimensionY) && valid(z2, dimensionZ)) {
                this.filters[x2][y2][z2].setSide(s.getOpposite(), cube.getTriangle(s).getOpposite());
            }
        }

        ICube tmp = this.cubes[x][y][z];
        this.operations.addLast(new SetOperation(x, y, z, tmp));
        this.cubes[x][y][z] = cube;
        return tmp;
    }

    private boolean valid(int coord, int max) {
        return coord >= 0 && coord < max;
    }

    public boolean undo() {
        SetOperation op = this.operations.pollLast();
        if (op == null) return false;
        for(ICube.Side s : ICube.Side.values()) {
            int x2 = op.x + s.x, y2 = op.y + s.y, z2 = op.z + s.z;
            if(valid(x2, dimensionX) && valid(y2, dimensionY) && valid(z2, dimensionZ)) {
                this.filters[x2][y2][z2].setSide(s.getOpposite(), Triangle.AnyNotNone);
            }
        }
        this.cubes[op.x][op.y][op.z] = op.previous;
        return true;
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

    /**
     * Contains an atomic operation made on this solution.
     */
    private record SetOperation(int x, int y, int z, ICube previous) {}
}
