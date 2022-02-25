package implementation.cube.sorter;

import abstractions.Orientation;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.ICubeSorter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * A data structure to store cubes and query for specific ones. Queries are cached using a HashMap.
 */
public class HashCubeSorter implements ICubeSorter {
    /** Wrapper for a query result. God, why doesn't this language support tuples... */
    private record QueryResult(int id, ICube[] cubes) {}

    private final HashMap<ICubeFilter, QueryResult[]> queries = new HashMap<>();
    /** Immutable */
    private final ICube[] given;

    protected HashCubeSorter(ICube[] cubes) {
        this.given = cubes;
    }

    @Override
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

    @Override
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

    @Override
    public int count(ICubeFilter matcher, Predicate<Integer> filter) {
        prepareMatching(matcher);
        int count = 0;
        for (QueryResult result : queries.get(matcher)) {
            if(filter.test(result.id)) {
                count += result.cubes.length;
            }
        }

        return count;
    }

    @Override
    public int unique(ICubeFilter f) {
        prepareMatching(f);
        return queries.get(f).length;
    }

    @Override
    public int getNumCachedQueries() {
        return this.queries.size();
    }

    @Override
    public int getNumCubes() {
        return this.given.length;
    }
}
