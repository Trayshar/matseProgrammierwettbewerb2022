package abstractions.cube;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;

/**
 * Interface for anything that can hold cubes
 */
public interface ICubeSet extends Iterable<ICube>, Cloneable {

    /**
     * Returns the amount of cubes in this set
     */
    int size();

    /**
     * Gets any of the cubes contained in this set
     */
    default ICube getAny() {
        return this.stream().findAny().orElse(null);
    }

    /**
     * Returns a stream over all cubes in this set
     */
    Stream<ICube> stream();

    /**
     * Returns a stream over all cubes matching the given filter
     */
    default Stream<ICube> matching(ICubeFilter filter) {
        return this.stream().filter(filter::match);
    }

    default Iterator<ICube> iterator() {
        return this.stream().iterator();
    }

    default Spliterator<ICube> spliterator() {
        return Spliterators.spliterator(this.iterator(), this.size(), 0);
    }
}
