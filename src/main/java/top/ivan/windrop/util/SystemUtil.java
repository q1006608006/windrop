package top.ivan.windrop.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;

import javax.swing.*;
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
    private SystemUtil() {
    }

    private static String systemKey;

    public static String getPCName() {
        String paName;
        try {
            paName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            paName = UUID.randomUUID().toString().replace("-", "");
        }
        return paName;
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
        } catch (Exception e) {
            e.printStackTrace();
            paMac = UUID.randomUUID().toString().replace("-", "");
        }
        return paMac;
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
                    if (inetAddress instanceof Inet4Address && testNetworkInterface(networkInterface)) { // IPV4
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

    private static boolean testNetworkInterface(NetworkInterface itf) {
        List<String> itfList = (List<String>) System.getProperties().getOrDefault("windrop.networkInterfaces", Collections.emptyList());
        if (itfList.isEmpty()) {
            return true;
        }
        return itfList.stream().anyMatch(t -> t.equalsIgnoreCase(itf.getDisplayName()));
    }

    public static String getDriveSN(String drive) {
        StringBuilder result = new StringBuilder();
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            try (FileWriter fw = new FileWriter(file)) {
                String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                        + "Set colDrives = objFSO.Drives\n"
                        + "Set objDrive = colDrives.item(\"" + drive + "\")\n"
                        + "Wscript.Echo objDrive.SerialNumber";  // see note
                fw.write(vbs);
            }
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = input.readLine()) != null) {
                    result.append(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString().trim();
    }

    public static String getMotherboardSN() {
        if (isWindows()) {
            StringBuilder result = new StringBuilder();
            try {
                File file = File.createTempFile("realhowto", ".vbs");
                file.deleteOnExit();
                try (FileWriter fw = new java.io.FileWriter(file)) {
                    String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                            + "Set colItems = objWMIService.ExecQuery _ \n"
                            + "   (\"Select * from Win32_BaseBoard\") \n"
                            + "For Each objItem in colItems \n"
                            + "   Wscript.Echo objItem.SerialNumber \n"
                            + "    exit for  ' do the first cpu only! \n" + "Next \n";

                    fw.write(vbs);
                }
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
        } else {
            return "unknown";
        }
    }

    public static String getCpuSN() {
        if (isWindows()) {
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"C:\\Windows\\System32\\wbem\\WMIC.exe", "cpu", "get", "ProcessorId"});
                process.getOutputStream().close();
                Scanner sc = new Scanner(process.getInputStream());
                sc.next();
                return sc.next();
            } catch (IOException e) {
                log.error("get cpu sn error", e);
                return "";
            }
        } else {
            return "unknown";
        }
    }

    public static String getSystemKey() {
        if (!StringUtils.hasLength(systemKey)) {
            String keyStr = String.join(";", SystemUtil.getPCName(), SystemUtil.getMotherboardSN(), SystemUtil.getCpuSN());
            systemKey = DigestUtils.md5Hex(keyStr);
        }
        return systemKey;
    }

    public static byte[] encrypt(byte[] data) {
        return ConvertUtil.encrypt(data, getSystemKey());
    }

    public static byte[] decrypt(byte[] data) {
        return ConvertUtil.decrypt(data, getSystemKey());
    }

    public static String getDocumentsPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setVisible(false);
        return chooser.getFileSystemView().getDefaultDirectory().toString();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows");
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("os.name"));
    }
}
