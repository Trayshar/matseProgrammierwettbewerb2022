package implementation.cube.sorter;

import abstractions.Orientation;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.ICubeSorter;
import implementation.Puzzle;
import implementation.cube.filter.ByteCubeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Predicate;

/**
 * A cube sorter using one huge array for indexing and with duplicate detection. Use only for larger puzzles,
 * otherwise the time spent allocating and null-ing the array will take longer than solving it.
 */
public class ArrayCubeSorter implements ICubeSorter {

    private int cachedQueries = 0;
    private final ICube[] cache;
    private final QuerySet[] queries = new QuerySet[46656]; // 6^6
    /** Immutable */
    private final ICube[] given;

    protected ArrayCubeSorter(ICube[] cubes) {
        this.given = cubes;
        this.cache = new ICube[cubes.length*24];
    }

    @Override
    public void cache(ICubeFilter filter) {
        this.cache(filter, filter.getUniqueId());
    }

    private QuerySet cache(ICubeFilter filter, int index) {
        assert filter.getUniqueId() == index;

        HashSet<Integer> usedUniqueIds = new HashSet<>();
        boolean hasDuplicates = false;
        ArrayList<QueryResult> results = new ArrayList<>();
        ArrayList<ICube> cubes = new ArrayList<>();
        for (ICube cube : this.given) {
            for(Orientation o : cube.match(filter)) {
                ICube c = cube.cloneCube();
                c.setOrientation(o);
                cubes.add(c);
            }

            if(!cubes.isEmpty()) {
                hasDuplicates |= usedUniqueIds.add(cube.getUniqueCubeId());
                results.add(new QueryResult(cube.getIdentifier(), cube.getUniqueCubeId(), cubes.toArray(new ICube[0])));
            }
            cubes.clear();
        }

        QuerySet tmp = new QuerySet(results.toArray(new QueryResult[0]), hasDuplicates);
        this.queries[index] = tmp;
        cachedQueries++;
        return tmp;
    }

    @Override
    public ICube[] matching(ICubeFilter matcher, Predicate<Integer> filter) {
        // Get current query
        int queryIndex = matcher.getUniqueId();
        QuerySet query = this.queries[queryIndex];
        if(query == null) query = this.cache(matcher, queryIndex);

        int cacheIndex = 0;
        if(query.hasDuplicates) {
            HashSet<Integer> usedUniqueIds = new HashSet<>();
            for (QueryResult result : query.results) {
                if(filter.test(result.id)) {
                    if(!usedUniqueIds.add(result.uniqueCubeId)) {
                        continue; // this cube is identical to one already returned; Skipping this cube
                    }
                    int length = result.cubes.length;
                    System.arraycopy(result.cubes, 0, this.cache, cacheIndex, length);
                    cacheIndex += length;
                }
            }
        }else{
            for (QueryResult result : query.results) {
                if(filter.test(result.id)) {
                    int length = result.cubes.length;
                    System.arraycopy(result.cubes, 0, this.cache, cacheIndex, length);
                    cacheIndex += length;
                }
            }
        }

        ICube[] result = new ICube[cacheIndex];
        System.arraycopy(this.cache, 0, result, 0, cacheIndex);
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
    public int unique(CubeType type) {
        int index = type.predicate.getUniqueId();
        QuerySet query = this.queries[index];
        if(query == null) query = this.cache(type.predicate, index);

        if(Puzzle.DEBUG) {
            int duplicates = 0;
            for (QueryResult result : query.results) {
                if(result.cubes.length > 0) {
                    if(this.unique(new ByteCubeFilter(result.cubes[0].getTriangles())) > 1) duplicates++;
                }
            }
            System.out.printf("Found %d duplicates in CubeType %s\n", duplicates, type);
        }

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

    /** Wrapper for a single cube in all orientations it matches the query in */
    private record QueryResult(int id, int uniqueCubeId, ICube[] cubes) {}

    /** Wrapper for multiple cubes in multiple orientations */
    private record QuerySet(QueryResult[] results, boolean hasDuplicates) {}
}
