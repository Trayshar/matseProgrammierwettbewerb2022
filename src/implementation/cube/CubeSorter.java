package implementation.cube;

import abstractions.Orientation;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * A data structure to store cubes and query for specific ones. Queries are cached.
 */
public class CubeSorter {
    /** Wrapper for a query result. God, why doesn't this language support tuples... */
    private record QueryResult(int id, ICube[] cubes) {}

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
        ArrayList<QueryResult> results = new ArrayList<>();
        ArrayList<ICube> cubes = new ArrayList<>();
        for (ICube cube : this.given) {
            ArrayList<Orientation> orientations = cube.match(filter);

            for(Orientation o : orientations) {
                ICube c = cube.cloneCube();
                c.setOrientation(o);
                cubes.add(c);
            }

            if(!cubes.isEmpty()) {
                results.add(new QueryResult(cube.getIdentifier(), cubes.toArray(new ICube[0])));
            }
            cubes.clear();
        }

        this.queries.put(filter.cloneFilter(), results.toArray(new QueryResult[0]));
    }

    private void prepareMatching(ICubeFilter filter) {
        if(!queries.containsKey(filter)) this.cache(filter);

//        if(Puzzle.DEBUG) {
//            Arrays.stream(queries.get(filter)).forEach(queryResult -> {
//                if (queryResult.cube.getNumTriangles() != filter.getNumTriangle()
//                        || Arrays.stream(queryResult.orientations)
//                        .anyMatch(orientation -> !filter.match(queryResult.cube, orientation))
//                ) {
//                    throw new IllegalStateException();
//                }
//            });
//        }
    }

    /**
     * Retrieves all cubes that match the given filter and who's ID satisfies the predicate in all their possible orientations.
     * The resulting array might be empty.
     */
    public ICube[] matching(ICubeFilter matcher, Predicate<Integer> filter) {
        prepareMatching(matcher);

        ArrayList<ICube> cubes = new ArrayList<>();
        for (QueryResult result : queries.get(matcher)) {
            if(filter.test(result.id)) {
                cubes.addAll(List.of(result.cubes));
            }
        }

        return cubes.toArray(new ICube[0]);
    }

    /**
     * Returns the amount of cubes that are of the given type.
     */
    public int unique(CubeType type) {
        prepareMatching(type.predicate);
        return queries.get(type.predicate).length;
    }

    /**
     * Returns the number of cached queries
     */
    public int getSize() {
        return this.queries.size();
    }

    /**
     * Returns the number of unique cubes this sorter has been initialized with
     */
    public int getNumCubes() {
        return this.given.length;
    }
}
