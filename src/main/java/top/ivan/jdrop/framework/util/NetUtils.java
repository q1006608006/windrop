package top.ivan.jdrop.framework.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Ivan
 * @since 2024/03/15 10:45
 */
public class NetUtils {
    public static String getLocalHostName() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            String hostname = localhost.getHostName();
            if (!hostname.endsWith(".local")) {
                hostname += ".local";
            }
            return hostname;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPCMac() {
        String paMac;
        try {
            NetworkInterface nif = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] bytes = nif.getHardwareAddress();
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                if (i != 0)
                    buffer.append("-");
                //bytes[i]&0xff将有符号byte数值转换为32位有符号整数，其中高24位为0，低8位为byte[i]
                int intMac = bytes[i] & 0xff;
                //toHexString函数将整数类型转换为无符号16进制数字
                String str = Integer.toHexString(intMac);
                buffer.append(str);
            }
            paMac = buffer.toString().toUpperCase();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return paMac;
    }

}
