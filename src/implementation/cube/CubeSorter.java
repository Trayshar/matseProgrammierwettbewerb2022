package implementation.cube;

import abstractions.Orientation;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import implementation.Puzzle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A data structure to store cubes and query for specific ones. Queries are cached.
 */
public class CubeSorter {
    /** Wrapper for a query result. God, why doesn't this language support tuples... */
    private record QueryResult(ICube cube, Orientation[] orientations) {
        public int getID() {
            return this.cube.getIdentifier();
        }

        public Stream<ICube> stream() {
            return Arrays.stream(orientations).map(orientation -> {
                ICube c = cube.cloneCube();
                c.setOrientation(orientation);
                return c;
            });
        }
    }

    private final HashMap<ICubeFilter, QueryResult[]> queries = new HashMap<>();
    /** Immutable */
    private final ICube[] given;

    public CubeSorter(ICube[] cubes) {
        this.given = cubes;
    }

    /**
     * Makes a request and caches the result for later use.
     */
    public void cache(ICubeFilter filter) {
        this.queries.put(filter.cloneFilter(),
                Arrays.stream(this.given)
                        .map(cube -> new QueryResult(cube, cube.match(filter).toArray(Orientation[]::new)))
                        .filter(queryResult -> queryResult.orientations.length != 0)
                        .toArray(QueryResult[]::new)
        );
    }

    private void prepareMatching(ICubeFilter filter) {
        if(!queries.containsKey(filter)) this.cache(filter);

        if(Puzzle.DEBUG) {
            Arrays.stream(queries.get(filter)).forEach(queryResult -> {
                if (queryResult.cube.getNumTriangles() != filter.getNumTriangle()
                        || Arrays.stream(queryResult.orientations)
                        .anyMatch(orientation -> !filter.match(queryResult.cube, orientation))
                ) {
                    throw new IllegalStateException();
                }
            });
        }
    }

    /**
     * Retrieves all cubes that match the given filter and who's ID satisfies the predicate.
     * The resulting array might be empty.
     */
    public ICube[] matching(ICubeFilter matcher, Predicate<Integer> filter) {
        prepareMatching(matcher);
        return Arrays.stream(queries.get(matcher))
                .filter(queryResult -> filter.test(queryResult.cube.getIdentifier()))
                .flatMap(QueryResult::stream)
                .toArray(ICube[]::new);
    }

    public int getSize() {
        return this.queries.size();
    }
}
