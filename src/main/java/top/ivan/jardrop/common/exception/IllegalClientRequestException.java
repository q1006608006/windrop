package top.ivan.jardrop.common.exception;

/**
 * @author Ivan
 * @since 2024/03/22 14:19
 */
public class IllegalClientRequestException extends Exception {
    public IllegalClientRequestException() {
        super();
    }

    public IllegalClientRequestException(String message) {
        super(message);
    }

    public IllegalClientRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalClientRequestException(Throwable cause) {
        super(cause);
    }
}
