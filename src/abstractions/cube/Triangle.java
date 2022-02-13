package abstractions.cube;

public enum Triangle {
    None, BottomLeft, UpperLeft, UpperRight, BottomRight, Any;

    public int serialize() {
        if(this == Any) throw new UnsupportedOperationException("Cannot serialize filter value \"Any\"!");
        return this.ordinal();
    }

    public static Triangle valueOf(int i) {
        assert i < Triangle.values().length;

        return Triangle.values()[i];
    }
}
