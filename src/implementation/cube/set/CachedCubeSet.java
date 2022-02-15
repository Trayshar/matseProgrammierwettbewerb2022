package implementation.cube.set;

import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.ICubeSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * A CubeSet that caches queries made to its content.
 */
public class CachedCubeSet implements ICubeSet {

    private final HashMap<ICubeFilter, ICube[]> map = new HashMap<>();
    private final int size;

    public CachedCubeSet(ICube[] cubes) {
        this.size = cubes.length;
        this.map.put(null, cubes);
    }

    /**
     * Caches the query
     */
    private void cache(ICubeFilter filter) {
        this.map.put(filter, (ICube[]) Arrays.stream(this.map.get(null)).filter(filter::match).toArray());
    }

    public Stream<ICube> matching(ICubeFilter filter) {
        if(!map.containsKey(filter)) this.cache(filter);
        return Arrays.stream(map.get(filter));
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Stream<ICube> stream() {
        return Arrays.stream(map.get(null));
    }
}
