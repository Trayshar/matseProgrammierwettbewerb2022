package abstractions;

/**
 * Quaternion orientation
 */
public enum Orientation{
    ;


    private final float q1, q2, q3, q4;

    Orientation(float q1, float q2, float q3, float q4) {
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
        this.q4 = q4;
    }

//    public Orientation rotate(Axis axis, int amount) {
//        int angle = amount % 4;
//        if(angle == 0) return this;
//
//        int x = this.x + axis.getX() * amount;
//        int y = this.y + axis.getY() * amount;
//        int z = this.z + axis.getZ() * amount;
//
//        return Orientation.get(x % 4, y % 3, z % 2);
//    }

    private static final Orientation[][][][] mappedValues = new Orientation[4][6][6][6];
    static {
        for (Orientation o : values()) {
            mappedValues[getIndex(o.q1)][getIndex(o.q2)][getIndex(o.q3)][getIndex(o.q4)] = o;
        }
    }

    private static Orientation get(float q1, float q2, float q3, float q4) {
        return mappedValues[getIndex(q1)][getIndex(q2)][getIndex(q3)][getIndex(q4)];
    }

    private static final float HalfSqrt2 = (float) (Math.sqrt(2) * 0.5d);

    private static int getIndex(float q) {
        return Math.round(q * q * 4f) * (q < 0 ? 3 : 1);
    }

    private static float getQuaternion(int index) {
        switch(index) {
            case 0: return 0f;
            case 1: return 0.5f;
            case 2: return HalfSqrt2;
            case 3: return -0.5f;
            case 4: return 1f;
            case 6: return -1f * HalfSqrt2;
            default: throw new UnsupportedOperationException("Illegal index \"" + index + "\"!");
        }
    }
}
