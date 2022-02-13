package abstractions.cube;

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
    ICube getAny();

    /**
     * Returns a stream over all cubes in this set
     */
    Stream<ICube> stream();

    default Spliterator<ICube> spliterator() {
        return Spliterators.spliterator(this.iterator(), this.size(), 0);
    }
}
