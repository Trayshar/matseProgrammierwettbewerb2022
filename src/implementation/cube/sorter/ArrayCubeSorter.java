package implementation.cube.sorter;

import abstractions.Orientation;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.ICubeSorter;

import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * A cube sorter using one huge array for indexing. Use only for larger puzzles,
 * otherwise the time spent allocating and null-ing the array will take longer than solving it.
 */
public class ArrayCubeSorter implements ICubeSorter {
    /** Wrapper for a query result. God, why doesn't this language support tuples... */
    private record QueryResult(int id, ICube[] cubes) {}

    private static class QuerySet {
        private final QueryResult[] results;
        private final int size;

        private QuerySet(QueryResult[] results) {
            this.results = results;
            this.size = calculateSize();
        }

        private QuerySet(QueryResult[] results, int size) {
            this.results = results;
            this.size = size;
            assert size == calculateSize();
        }

        private int calculateSize() {
            int i = 0;
            for(QueryResult r : results) {
                i += r.cubes.length;
            }
            return i;
        }
    }

    private int cachedQueries = 0;
    private final QuerySet[] queries = new QuerySet[46656]; // 6^6
    /** Immutable */
    private final ICube[] given;

    protected ArrayCubeSorter(ICube[] cubes) {
        this.given = cubes;
    }

    @Override
    public void cache(ICubeFilter filter) {
        this.cache(filter, filter.getUniqueId());
    }

    private QuerySet cache(ICubeFilter filter, int index) {
        assert filter.getUniqueId() == index;

        ArrayList<QueryResult> results = new ArrayList<>();
        ArrayList<ICube> cubes = new ArrayList<>();
        int size = 0;
        for (ICube cube : this.given) {
            for(Orientation o : cube.match(filter)) {
                ICube c = cube.cloneCube();
                c.setOrientation(o);
                cubes.add(c);
            }

            if(!cubes.isEmpty()) {
                results.add(new QueryResult(cube.getIdentifier(), cubes.toArray(new ICube[0])));
            }
            size += cubes.size();
            cubes.clear();
        }

        QuerySet tmp = new QuerySet(results.toArray(new QueryResult[0]), size);
        this.queries[index] = tmp;
        cachedQueries++;
        return tmp;
    }

    @Override
    public ICube[] matching(ICubeFilter matcher, Predicate<Integer> filter) {
        // Get current query
        int index = matcher.getUniqueId();
        QuerySet query = this.queries[index];
        if(query == null) query = this.cache(matcher, index);

        // Go through all results
        int i = 0;
        ICube[] cubes = new ICube[query.size];
        for (QueryResult result : query.results) {
            if(filter.test(result.id)) {
                int length = result.cubes.length;
                System.arraycopy(result.cubes, 0, cubes, i, length);
                i += length;
            }
        }

        ICube[] result = new ICube[i];
        System.arraycopy(cubes, 0, result, 0, i);
        return result;
    }

    @Override
    public int count(ICubeFilter matcher, Predicate<Integer> filter) {
        int index = matcher.getUniqueId();
        QuerySet query = this.queries[index];
        if(query == null) query = this.cache(matcher, index);

        int count = 0;
        for (QueryResult result : query.results) {
            if(filter.test(result.id)) {
                count += result.cubes.length;
            }
        }

        return count;
    }

    @Override
    public int unique(ICubeFilter f) {
        int index = f.getUniqueId();
        QuerySet query = this.queries[index];
        if(query == null) query = this.cache(f, index);

        return query.results.length;
    }

    @Override
    public int getNumCachedQueries() {
        return this.cachedQueries;
    }

    @Override
    public int getNumCubes() {
        return this.given.length;
    }
}
