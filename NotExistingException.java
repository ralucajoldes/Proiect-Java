package socialnetwork.exceptions;

public class NotExistingException extends RuntimeException
{
    public NotExistingException() {
    }

    public NotExistingException(String message) {
        super(message);
    }

    public NotExistingException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotExistingException(Throwable cause) {
        super(cause);
    }

    public NotExistingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
