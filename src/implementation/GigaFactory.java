package implementation;

import abstractions.cube.ICube;
import abstractions.IPuzzleSolver;

public final class GigaFactory {
    private GigaFactory() {}

    public static ICube readFromRaw(String raw) {
        System.out.println(raw);
        return null;
    }

    public static IPuzzleSolver constructSolver() {
        return (dimensionX, dimensionY, dimensionZ, cubes) -> null;
    }
}
