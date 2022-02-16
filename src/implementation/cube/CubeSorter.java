package implementation.cube;

import abstractions.Orientation;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * A data structure to store cubes and query for specific ones. Queries are cached.
 */
public class CubeSorter {
    /** Wrapper for a query result. God, why doesn't this language support tuples... */
    public record QueryResult(ICube cube, Orientation[] orientations) {}

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
        this.queries.put(filter,
                Arrays.stream(this.given)
                        .map(cube -> new QueryResult(cube, cube.match(filter).toArray(Orientation[]::new)))
                        .filter(queryResult -> queryResult.orientations.length != 0)
                        .toArray(QueryResult[]::new)
        );
    }

    /**
     * Retrieves the result of a query. The resulting stream might be empty.
     */
    public Stream<QueryResult> matching(ICubeFilter filter) {
        if(!queries.containsKey(filter)) this.cache(filter);
        return Arrays.stream(queries.get(filter));
    }
}
