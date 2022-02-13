package abstractions;

public enum Axis {
    X(1, 0, 0), Y(0, 1, 0), Z(0, 0, 1);

    private final int x, y, z;

    Axis(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
