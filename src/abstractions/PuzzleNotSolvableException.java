package abstractions;

public class PuzzleNotSolvableException extends Exception {
    public PuzzleNotSolvableException() {}

    public PuzzleNotSolvableException(String message) {
        super(message);
    }

    public PuzzleNotSolvableException(String message, Throwable cause) {
        super(message, cause);
    }
}
