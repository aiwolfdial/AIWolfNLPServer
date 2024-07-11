package server;

public class ActionTimeoutException extends Exception {
    private static final long serialVersionUID = 1L;

    public ActionTimeoutException() {
        super();
    }

    public ActionTimeoutException(String message) {
        super(message);
    }

    public ActionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionTimeoutException(Throwable cause) {
        super(cause);
    }
}
