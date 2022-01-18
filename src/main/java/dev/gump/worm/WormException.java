package dev.gump.worm;

public class WormException extends RuntimeException {

    public WormException() {}

    public WormException(String message) {
        super(message);
    }

    public WormException(Throwable cause) {
        super(cause);
    }

    public WormException(String message, Throwable cause) {
        super(message, cause);
    }
}
