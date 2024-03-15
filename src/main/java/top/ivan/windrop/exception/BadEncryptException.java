package top.ivan.windrop.exception;

/**
 * @author Ivan
 * @since 2021/05/28 17:40
 */
public class BadEncryptException extends RuntimeException {
    public BadEncryptException() {
        super();
    }

    public BadEncryptException(String message) {
        super(message);
    }

    public BadEncryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadEncryptException(Throwable cause) {
        super(cause);
    }

    protected BadEncryptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
