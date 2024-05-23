package top.ivan.jdrop.framework.util;

import org.apache.commons.codec.binary.Base64;

public class Base64Utils {

    public static String encodeBase64(byte[] data) {
        return Base64.encodeBase64String(data);
    }

    public static byte[] decodeBase64(String data) {
        return Base64.decodeBase64(data);
    }

    public static String decodeBase64Str(String data) {
        return new String(decodeBase64(data));
    }


}
