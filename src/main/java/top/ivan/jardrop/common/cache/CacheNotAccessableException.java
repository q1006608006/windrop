package top.ivan.jardrop.common.cache;

/**
 * @author Ivan
 * @description
 * @date 2021/3/26
 */
public class CacheNotAccessableException extends Exception {
    public static CacheNotAccessableException TIME_OUT = new CacheNotAccessableException("二维码已失效");
    public static CacheNotAccessableException OVER_LIMIT = new CacheNotAccessableException("二维码使用上限");
    public static CacheNotAccessableException UNKNOWN = new CacheNotAccessableException("未知来源");
    public static CacheNotAccessableException FAILED = new CacheNotAccessableException("生成二维码失败");

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
