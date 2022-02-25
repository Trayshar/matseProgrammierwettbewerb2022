package implementation.cube.sorter;

import abstractions.Orientation;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.ICubeSorter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ArrayCubeSorter implements ICubeSorter {
    /** Wrapper for a query result. God, why doesn't this language support tuples... */
    private record QueryResult(int id, ICube[] cubes) {}
    private record QuerySet(QueryResult[] results) {}

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
        for (ICube cube : this.given) {
            for(Orientation o : cube.match(filter)) {
                ICube c = cube.cloneCube();
                c.setOrientation(o);
                cubes.add(c);
            }

            if(!cubes.isEmpty()) {
                results.add(new QueryResult(cube.getIdentifier(), cubes.toArray(new ICube[0])));
            }
            cubes.clear();
        }

        QuerySet tmp = new QuerySet(results.toArray(new QueryResult[0]));
        this.queries[index] = tmp;
        cachedQueries++;
        return tmp;
    }

    @Override
    public ICube[] matching(ICubeFilter matcher, Predicate<Integer> filter) {
        int index = matcher.getUniqueId();
        QuerySet query = this.queries[index];
        if(query == null) query = this.cache(matcher, index);

        ArrayList<ICube> cubes = new ArrayList<>();
        for (QueryResult result : query.results) {
            if(filter.test(result.id)) {
                cubes.addAll(List.of(result.cubes));
            }
        }

        return cubes.toArray(new ICube[0]);
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
