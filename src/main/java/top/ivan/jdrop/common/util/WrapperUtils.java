package top.ivan.jdrop.common.util;

public class WrapperUtils {

    public static boolean isTrue(Object obj) {
        return Boolean.TRUE.equals(obj);
    }

    public static boolean isFalse(Object obj) {
        return !isTrue(obj);
    }

}
