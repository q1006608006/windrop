package top.ivan.windrop.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Ivan
 * @description
 * @date 2021/3/11
 */
@Slf4j
public class SystemUtil {
    private static String SYSTEM_KEY;

    public static String getPCName() {
        String PC_NAME;
        try {
            PC_NAME = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            PC_NAME = UUID.randomUUID().toString().replace("-", "");
        }
        return PC_NAME;
    }

    public static String getPCMac() {
        String PC_MAC;
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
            PC_MAC = UUID.randomUUID().toString().replace("-", "");
        }
        return PC_MAC;
    }

    public static List<String> getLocalIPList() {
        List<String> ipList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress instanceof Inet4Address) { // IPV4
                        ip = inetAddress.getHostAddress();
                        ipList.add(ip);
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ipList;
    }

    public static String getDriveSN(String drive) {
        StringBuilder result = new StringBuilder();
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            System.out.println(file.getAbsolutePath());
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                    + "Set colDrives = objFSO.Drives\n"
                    + "Set objDrive = colDrives.item(\"" + drive + "\")\n"
                    + "Wscript.Echo objDrive.SerialNumber";  // see note
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result.append(line);
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString().trim();
    }

    public static String getMotherboardSN() {
        StringBuilder result = new StringBuilder();
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n"
                    + "   (\"Select * from Win32_BaseBoard\") \n"
                    + "For Each objItem in colItems \n"
                    + "   Wscript.Echo objItem.SerialNumber \n"
                    + "    exit for  ' do the first cpu only! \n" + "Next \n";

            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec(
                    "cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result.append(line);
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString().trim();
    }

    public static String getCpuSN() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"wmic", "cpu", "get", "ProcessorId"});
            process.getOutputStream().close();
            Scanner sc = new Scanner(process.getInputStream());
            sc.next();
            return sc.next();
        } catch (IOException e) {
            log.error("get cpu sn error", e);
            return "";
        }
    }

    public static String getSystemKey() {
        if (StringUtils.isEmpty(SYSTEM_KEY)) {
            String keyStr = String.join(";", SystemUtil.getPCName(), SystemUtil.getMotherboardSN(), SystemUtil.getCpuSN());
            SYSTEM_KEY = DigestUtils.md5Hex(keyStr);
        }
        return SYSTEM_KEY;
    }

    public static byte[] encrypt(byte[] data) {
        return ConvertUtil.encrypt(data, getSystemKey());
    }

    public static byte[] decrypt(byte[] data) {
        return ConvertUtil.decrypt(data, getSystemKey());
    }

}
