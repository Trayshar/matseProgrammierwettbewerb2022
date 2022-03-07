package abstractions.cube;

public enum Triangle {
    None, BottomLeft, TopLeft, TopRight, BottomRight, AnyNotNone;

    public int serialize() {
        if(this == AnyNotNone) throw new UnsupportedOperationException("Cannot serialize filter value \"" + this + "\"!");
        return this.ordinal();
    }

    private static final Triangle[] matchingHorizontal = {None, TopLeft, BottomLeft, BottomRight, TopRight, AnyNotNone};
    private static final Triangle[] matchingVertical   = {None, BottomRight, TopRight, TopLeft, BottomLeft, AnyNotNone};

    public Triangle getMatching(boolean isVertical) {
        if(isVertical) {
            return matchingVertical[this.ordinal()];
        }
        return matchingHorizontal[this.ordinal()];
    }

    private static final byte[] matchingHorizontalId = {0, 2, 1, 4, 3, 5};
    private static final byte[] matchingVerticalId   = {0, 4, 3, 2, 1, 5};

    public static byte getMatching(int triangle, boolean isVertical) {
        if(isVertical) {
            return matchingVerticalId[triangle];
        }
        return matchingHorizontalId[triangle];
    }

    /**
     * Enum.values() clones the array to stop modification of the enum data (God, I sometimes hate this langauge).
     * We need performance, so we don't care about safety.
     */
    private static final Triangle[] values = values();

    /**
     * Faster brother of Enum.values(). DO NOT MODIFY THE RETURNED ARRAY
     */
    public static Triangle[] getValues() {
        return values;
    }

    /**
     * Returns the orientation for the given ordinal.
     */
    public static Triangle valueOf(int ordinal) {
        assert ordinal >= 0 && ordinal < values.length;

        return values[ordinal];
    }
}
