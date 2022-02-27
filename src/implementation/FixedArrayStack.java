package implementation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

/**
 * A messy Stack implementation using a fixed-size array. Indexing beyond that is unchecked and will throw a {@link IndexOutOfBoundsException}.
 * Messy meaning it won't clean up removed elements by default, so they stay in memory until they are overwritten.
 * Use the removeLastTidily if you want to clean after yourself.
 */
public class FixedArrayStack<T> implements Deque<T> {
    private final T[] data;
    /** Index of the first value */
    private int index = -1;

    public FixedArrayStack(T[] data) {
        this.data = data;
    }

    /**
     * Removes and returns the last element while deleting the reference to it.
     */
    public T removeLastTidily() {
        T tmp = data[index];
        data[index] = null;
        index--;
        return tmp;
    }

    @Override
    public void addFirst(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLast(T t) {
        data[++index] = t;
    }

    @Override
    public boolean offerFirst(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offerLast(T t) {
        if(index >= data.length) return false;
        data[++index] = t;
        return true;
    }

    @Override
    public T removeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T removeLast() {
        return data[index--];
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        if(index < 0) return null;
        return data[index--];
    }

    @Override
    public T getFirst() {
        return data[0];
    }

    @Override
    public T getLast() {
        return data[index];
    }

    @Override
    public T peekFirst() {
        return data[0];
    }

    @Override
    public T peekLast() {
        return data[index];
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offer(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T poll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T element() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T peek() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        throw new UnsupportedOperationException("Use offer on each element!");
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        index = -1;
    }

    @Override
    public void push(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return index + 1;
    }

    @Override
    public boolean isEmpty() {
        return index < 0;
    }

    @Override
    public Iterator<T> iterator() {
        return Arrays.stream(this.data).iterator();
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(this.data, index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> U[] toArray(U[] a) {
        return (U[]) Arrays.copyOf(this.data, index);
    }

    @Override
    public Iterator<T> descendingIterator() {
        throw new UnsupportedOperationException();
    }
}
