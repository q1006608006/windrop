package top.ivan.jardrop.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Ivan
 * @since 2024/03/15 10:45
 */
public class NetUtils {
    public static String getLocalHostName() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
