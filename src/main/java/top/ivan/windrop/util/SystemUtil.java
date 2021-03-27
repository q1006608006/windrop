package top.ivan.windrop.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.UUID;

/**
 * @author Ivan
 * @description
 * @date 2021/3/11
 */
public class SystemUtil {
    private static String PC_NAME;
    private static String PC_MAC;

    public static String getPCName() {
        if (StringUtils.isEmpty(PC_NAME)) {
            try {
                PC_NAME = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                PC_NAME = UUID.randomUUID().toString().replace("-", "");
            }
        }
        return PC_NAME;
    }

    public static String getPCMac() {
        if (StringUtils.isEmpty(PC_MAC)) {
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
                PC_MAC = buffer.toString().toUpperCase();
            } catch (Exception e) {
                e.printStackTrace();
                PC_NAME = UUID.randomUUID().toString().replace("-", "");
            }
        }
        return PC_MAC;
    }

}
