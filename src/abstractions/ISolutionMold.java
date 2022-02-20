package abstractions;

import implementation.cube.CubeSorter;

public interface ISolutionMold {
    Coordinate getPosition(int stage);

    boolean isSolvable(CubeSorter sorter);
}
