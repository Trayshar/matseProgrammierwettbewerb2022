package abstractions.cube;

import java.util.function.Predicate;

/**
 * Interface for anything that can hold cubes
 */
public interface ICubeSorter {

    /**
     * Makes a request and caches the result for later use.
     */
    void cache(ICubeFilter filter);

    /**
     * Retrieves the number of cubes (in all their possible orientations) that match the given filter and who's ID satisfies the predicate
     */
    int count(ICubeFilter matcher, Predicate<Integer> filter);

    /**
     * Retrieves the number of cubes (in all their possible orientations) that match the given filter.
     */
    default int count(ICubeFilter matcher) {
        return count(matcher, integer -> true);
    }

    /**
     * Retrieves all cubes (in all their possible orientations) that match the given filter and who's ID satisfies the predicate
     * The resulting array might be empty.
     */
    ICube[] matching(ICubeFilter matcher, Predicate<Integer> filter);

    /**
     * Retrieves all cubes (in all their possible orientations) that match the given filter.
     * The resulting array might be empty.
     */
    default ICube[] matching(ICubeFilter matcher) {
        return matching(matcher, integer -> true);
    }

    /**
     * Returns the amount of cubes that match the given filter uniquely
     */
    int unique(ICubeFilter f);

    /**
     * Returns the amount of cubes that are of the given type.
     */
    default int unique(CubeType type) {
        return unique(type.predicate);
    }

    /**
     * Returns the number of cached queries
     */
    int getNumCachedQueries();

    /**
     * Returns the number of unique cubes this sorter has been initialized with
     */
    int getNumCubes();
}
