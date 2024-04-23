package top.ivan.jdrop.common.cache;

/**
 * @author Ivan
 * @description
 * @date 2021/3/26
 */
public class CacheNotAccessableException extends Exception {

    public CacheNotAccessableException() {
        super();
    }

    public CacheNotAccessableException(String message) {
        super(message);
    }

    public CacheNotAccessableException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheNotAccessableException(Throwable cause) {
        super(cause);
    }

    protected CacheNotAccessableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
