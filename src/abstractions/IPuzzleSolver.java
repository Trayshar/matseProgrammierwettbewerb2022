package abstractions;

import java.util.concurrent.Callable;

public interface IPuzzleSolver extends Callable<IPuzzleSolution>, Runnable {
    /**
     * Solves the puzzle on the calling thread, Returning the solution once it's available
     * @throws PuzzleNotSolvableException If no solution could be found
     */
    IPuzzleSolution solve() throws PuzzleNotSolvableException;

    /**
     * Solves the puzzle using an Executor. This function must handle interrupts.
     * @throws PuzzleNotSolvableException If no solution could be found
     */
    IPuzzleSolution solveConcurrent() throws PuzzleNotSolvableException;

    /**
     * Returns the current status of this solver. May be called from another thread.
     * Must handle interrupts.
     */
    String getCurrentStatus();

    /**
     * Concurrently solve the puzzle.
     */
    default IPuzzleSolution call() throws PuzzleNotSolvableException {
        return solveConcurrent();
    }

    /**
     * Concurrently run this solver in "Observer mode", where status info is printed periodically
     */
    default void run() {
        System.out.println(getCurrentStatus());
    }
}
