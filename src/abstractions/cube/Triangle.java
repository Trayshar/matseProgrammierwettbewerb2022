package abstractions.cube;

public enum Triangle {
    None, BottomLeft, TopLeft, TopRight, BottomRight, Any, AnyNotNone;

    public int serialize() {
        if(this == Any || this == AnyNotNone) throw new UnsupportedOperationException("Cannot serialize filter value \"" + this + "\"!");
        return this.ordinal();
    }

    public Triangle getOpposite() {
        switch (this) {
            case None -> {
                return None;
            }
            case BottomLeft -> {
                return TopRight;
            }
            case TopLeft -> {
                return BottomRight;
            }
            case TopRight -> {
                return BottomLeft;
            }
            case BottomRight -> {
                return TopLeft;
            }
            case Any -> {
                // Throw exception here?
                return Any;
            }
            case AnyNotNone -> {
                // Throw exception here?
                return AnyNotNone;
            }
        }

        // This should not happen
        throw new UnsupportedOperationException("Triangle " + this + " does not exist!");
    }

    /**
     * Enum.values() clones the array to stop modification of the enum data (God, I sometimes hate this langauge).
     * We need performance, so we don't care about safety.
     */
    private static final Triangle[] values = values();

    /**
     * Faster brother of Enum.values(). DO NOT MODIFY THE VALUES RETURNED
     */
    public static Triangle[] getValues() {
        return values;
    }

    /**
     * Returns the orientation for the given ordinal. DO NOT MODIFY THE VALUE RETURNED
     */
    public static Triangle valueOf(int ordinal) {
        assert ordinal >= 0 && ordinal < values.length;

        return values[ordinal];
    }
}
