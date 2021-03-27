package top.ivan.windrop.ex;

/**
 * @author Ivan
 * @description
 * @date 2021/3/26
 */
public class CacheTimeoutException extends Exception{
    public CacheTimeoutException() {
        super();
    }

    public CacheTimeoutException(String message) {
        super(message);
    }

    public CacheTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheTimeoutException(Throwable cause) {
        super(cause);
    }

    protected CacheTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
