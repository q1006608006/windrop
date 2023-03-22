package top.ivan.windrop.util;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    private static void zip(ZipOutputStream out, File f, String base)
            throws IOException { // 方法重载
        if (f.isDirectory()) { // 测试此抽象路径名表示的文件是否是一个目录
            File[] fl = f.listFiles(); // 获取路径数组
            out.putNextEntry(new ZipEntry(base + "/")); // 写入此目录的entry
            base = base.length() == 0 ? "" : base + "/"; // 判断参数是否为空
            for (int i = 0; i < fl.length; i++) { // 循环遍历数组中文件
                zip(out, fl[i], base + fl[i].getName());
            }
        } else {
            out.putNextEntry(new ZipEntry(base)); // 创建新的进入点
            // 创建FileInputStream对象
            InputStream in = new FileInputStream(f);
            in = new BufferedInputStream(in);
            int b; // 定义int型变量
            byte[] buff = new byte[4096];
            while ((b = in.read(buff)) != -1) {
                out.write(buff, 0, b);
            }
            in.close(); // 关闭流
        }
    }

    public static void nioZip(File src, File output) throws IOException {
        ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(output.toPath()));
        WritableByteChannel outChannel = Channels.newChannel(out);
        nioZip(outChannel, out, src, src.getName());
        outChannel.close();
        out.close();
    }

    public static void nioZip(WritableByteChannel outChannel, ZipOutputStream out, File f, String base) throws IOException {
        BasicFileAttributes bfa = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
        if (bfa.isDirectory()) { // 测试此抽象路径名表示的文件是否是一个目录
            File[] fl = f.listFiles(); // 获取路径数组
            if (null == fl) {
                return;
            }
            out.putNextEntry(new ZipEntry(base + "/")); // 写入此目录的entry
            base = base.length() == 0 ? "" : base + "/"; // 判断参数是否为空
            for (File file : fl) { // 循环遍历数组中文件
                nioZip(outChannel, out, file, base + file.getName());
            }
        } else {
            out.putNextEntry(new ZipEntry(base)); // 创建新的进入点
            // 创建FileInputStream对象
            try (FileInputStream fileIn = new FileInputStream(f)) {
                FileChannel fileChannel = fileIn.getChannel();
                fileChannel.transferTo(0, bfa.size(), outChannel);
                fileChannel.close();
            }
        }
    }

}
