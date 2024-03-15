package top.ivan.windrop.exception;

/**
 * @author Ivan
 * @description
 * @date 2021/3/26
 */
public class CacheNotAccessException extends Exception{
    public CacheNotAccessException() {
        super();
    }

    public CacheNotAccessException(String message) {
        super(message);
    }

    public CacheNotAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheNotAccessException(Throwable cause) {
        super(cause);
    }

    protected CacheNotAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
