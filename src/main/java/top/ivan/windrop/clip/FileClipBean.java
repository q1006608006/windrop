package top.ivan.windrop.clip;

import java.awt.datatransfer.Transferable;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileClipBean implements ClipBean, FileBean {
    private byte[] data;
    private final File src;
    private final long updateTime;

    public FileClipBean(File file, long updateTime) {
        this.src = file;
        this.updateTime = updateTime;
    }

    @Override
    public synchronized byte[] getBytes() throws IOException {
        if (data == null) {
            if (src.isDirectory()) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ZipOutputStream out = new ZipOutputStream(bout);
                nioZip(out, src, src.getName());
                out.close();
                this.data = bout.toByteArray();
            } else {
                this.data = Files.readAllBytes(Paths.get(src.toURI()));
            }
        }
        return data;
    }

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

    private static void nioZip(ZipOutputStream out, File f, String base)
            throws IOException { // 方法重载
        WritableByteChannel outChannel = Channels.newChannel(out);
        nioZip(outChannel, out, f, base);
        outChannel.close();
    }

    private static void nioZip(WritableByteChannel outChannel, ZipOutputStream out, File f, String base) throws IOException {
        if (f.isDirectory()) { // 测试此抽象路径名表示的文件是否是一个目录
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
            FileInputStream fileIn = new FileInputStream(f);
            FileChannel fileChannel = fileIn.getChannel();
            fileChannel.transferTo(0, f.length(), outChannel);
            fileChannel.close();
            fileIn.close(); // 关闭流
        }
    }

    @Override
    public Transferable toTransferable() {
        return new FileSelection(Collections.singletonList(src));
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public boolean isOrigin(Object target) {
        return src.equals(target);
    }

    @Override
    public String getFileName() {
        return src.getName();
    }

    @Override
    public File getFile() {
        return src;
    }

    public long getLength() {
        if (src.isDirectory()) {
            return dirLength(src);
        } else {
            return src.length();
        }
    }

    private static long dirLength(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            long len = 0;
            for (File file : files) {
                if (file.isDirectory()) {
                    len += dirLength(file);
                } else {
                    len += file.length();
                }
            }
            return len;
        } else {
            return 0;
        }
    }

}
