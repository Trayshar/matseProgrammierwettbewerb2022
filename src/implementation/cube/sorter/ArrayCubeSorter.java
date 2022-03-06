package implementation.cube.sorter;

import abstractions.Orientation;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.ICubeSorter;
import implementation.Puzzle;
import implementation.cube.filter.ByteCubeFilter;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * A cube sorter using one huge array for indexing and with duplicate detection. Use only for larger puzzles,
 * otherwise the time spent allocating and null-ing the array will take longer than solving it.
 */
public class ArrayCubeSorter implements ICubeSorter, Cloneable {

    private int cachedQueries = 0;
    /*
     * Internal caches for writes. Not to be synchronized. Java doesn't have "Zero Cost Abstractions" , so we have to stick to simple data structures like arrays.
     */
    private ICube[] cubeCache;
    private int[] idCache;
    private QueryResult[] resultCache;
    /**
     * Since these queries are stateless no conflicting information is written. So no need to synchronize this thing. What could go wrong...
     */
    public static final QuerySet[] queries = new QuerySet[46656]; // 6^6
    /** Immutable */
    private final ICube[] given;

    protected ArrayCubeSorter(ICube[] cubes) {
        this.given = cubes;
        this.cubeCache = new ICube[given.length * 24];
        this.idCache = new int[given.length];
        this.resultCache = new QueryResult[given.length];
    }

    @Override
    public void cache(ICubeFilter filter) {
        this.cache(filter, filter.getUniqueId());
    }

    /**
     * Looks inside the idCache from index 0 to "idCacheLength"-1 for the id "idToCheck".
     * If it is found true is returned.
     */
    private boolean checkIfIdIsAlreadyUsed(int idCacheLength, int idToCheck) {
        for (int i = 0; i < idCacheLength; i++) {
            if(idCache[i] == idToCheck) {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks inside the cubeCache from index 0 to "cubeCacheIndex"-1 for a cube with the exact same triangles as "cubeToCheck" in Orientation "o".
     * If it is found true is returned.
     */
    private boolean checkIfOrientationIsAlreadyUsed(int cubeCacheIndex, ICube cubeToCheck, int o) {
        for (int i = 0; i < cubeCacheIndex; i++) {
            if(Arrays.equals(cubeCache[i].getTriangles(), cubeToCheck.getTriangles(o))) {
                return true;
            }
        }
        return false;
    }

    private QuerySet cache(ICubeFilter filter, int index) {
        assert filter.getUniqueId() == index;

        boolean hasDuplicates = false;
        int cubeCacheIndex = 0, resultCacheIndex = 0, idCacheIndex = 0;
        for (ICube cube : this.given) {
            for(int o = 0; o < 24; o++) {
                if(filter.match(cube.getTriangles(o)) && !checkIfOrientationIsAlreadyUsed(cubeCacheIndex, cube, o)) {
                    ICube c = cube.cloneCube();
                    c.setOrientation(Orientation.get(o));
                    cubeCache[cubeCacheIndex++] = c;
                }
            }

            if(cubeCacheIndex > 0) {
                int idToCheck = cube.getUniqueCubeId();
                if(checkIfIdIsAlreadyUsed(idCacheIndex, idToCheck)) {
                    hasDuplicates = true;
                }else {
                    idCache[idCacheIndex++] = idToCheck;
                }
                ICube[] resultCubes = new ICube[cubeCacheIndex];
                System.arraycopy(cubeCache, 0, resultCubes, 0, cubeCacheIndex);
                resultCache[resultCacheIndex++] = new QueryResult(cube.getIdentifier(), idToCheck, resultCubes);
            }
            cubeCacheIndex = 0;
        }

        QueryResult[] results = new QueryResult[resultCacheIndex];
        System.arraycopy(resultCache, 0, results, 0, resultCacheIndex);
        QuerySet tmp = new QuerySet(results, hasDuplicates);
        queries[index] = tmp;
        cachedQueries++;
        return tmp;
    }

    @Override
    public ICube[] matching(ICubeFilter matcher, Predicate<Integer> filter) {
        // Get current query
        int queryIndex = matcher.getUniqueId();
        QuerySet query = queries[queryIndex];
        if(query == null) query = this.cache(matcher, queryIndex);

        if(query.hasDuplicates) {
            return matchingDuplicate(query, filter);
        }else{
            return matchingSingle(query, filter);
        }
    }

    private ICube[] matchingDuplicate(QuerySet query, Predicate<Integer> filter) {
        int cubeCacheIndex = 0, idCacheIndex = 0;
        for (QueryResult result : query.results) {
            if(filter.test(result.id)) {
                int idToCheck = result.uniqueCubeId;
                if(checkIfIdIsAlreadyUsed(idCacheIndex, idToCheck)) {
                    continue;
                }else {
                    idCache[idCacheIndex++] = idToCheck;
                }
                int length = result.cubes.length;
                System.arraycopy(result.cubes, 0, this.cubeCache, cubeCacheIndex, length);
                cubeCacheIndex += length;
            }
        }

        ICube[] result = new ICube[cubeCacheIndex];
        System.arraycopy(this.cubeCache, 0, result, 0, cubeCacheIndex);
        return result;
    }

    private ICube[] matchingSingle(QuerySet query, Predicate<Integer> filter) {
        int cacheIndex = 0;
        for (QueryResult result : query.results) {
            if(filter.test(result.id)) {
                int length = result.cubes.length;
                System.arraycopy(result.cubes, 0, this.cubeCache, cacheIndex, length);
                cacheIndex += length;
            }
        }

        ICube[] result = new ICube[cacheIndex];
        System.arraycopy(this.cubeCache, 0, result, 0, cacheIndex);
        return result;
    }

    /**
     * Returns all orientations of any one cube that the given filter matches
     */
    public ICube[] matchingAny(ICubeFilter filter) {
        int queryIndex = filter.getUniqueId();
        QuerySet query = queries[queryIndex];
        if(query == null) query = this.cache(filter, queryIndex);

        if(query.results.length == 0) return new ICube[0];
        return Arrays.copyOf(query.results[0].cubes, query.results[0].cubes.length);
    }

    @Override
    public int count(ICubeFilter matcher, Predicate<Integer> filter) {
        int index = matcher.getUniqueId();
        QuerySet query = queries[index];
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
        QuerySet query = queries[index];
        if(query == null) query = this.cache(f, index);

        return query.results.length;
    }

    @Override
    public int unique(CubeType type) {
        int index = type.predicate.getUniqueId();
        QuerySet query = queries[index];
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

    public ArrayCubeSorter clone() {
        try {
            ArrayCubeSorter clone = (ArrayCubeSorter) super.clone();
            clone.cubeCache = new ICube[given.length * 24];
            clone.idCache = new int[given.length];
            clone.resultCache = new QueryResult[given.length];
            return clone;
        } catch (CloneNotSupportedException e) { // Should not happen.
            throw new UnsupportedOperationException(e);
        }
    }

    /** Wrapper for a single cube in all orientations it matches the query in */
    private record QueryResult(int id, int uniqueCubeId, ICube[] cubes) {}

    /** Wrapper for multiple cubes in multiple orientations */
    private record QuerySet(QueryResult[] results, boolean hasDuplicates) {}
}
