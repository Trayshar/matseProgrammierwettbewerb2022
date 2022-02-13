package abstractions.cube;

public interface ICubeFilter {
    /**
     * Checks if the given cube matches this filter
     */
    default boolean match(ICube cube){
        return this.match(
                (byte) cube.getTriangle(ICube.Side.Up).ordinal(),
                (byte) cube.getTriangle(ICube.Side.Left).ordinal(),
                (byte) cube.getTriangle(ICube.Side.Front).ordinal(),
                (byte) cube.getTriangle(ICube.Side.Right).ordinal(),
                (byte) cube.getTriangle(ICube.Side.Back).ordinal(),
                (byte) cube.getTriangle(ICube.Side.Down).ordinal()
        );
    }

    /**
     * Checks if the given cube matches this filter
     */
    boolean match(byte... triangles);
}
