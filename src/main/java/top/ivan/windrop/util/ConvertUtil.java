package top.ivan.windrop.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import top.ivan.windrop.clip.FileClipBean;
import top.ivan.windrop.ex.BadEncryptException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.EnumMap;
import java.util.concurrent.Executor;

/**
 * @author Ivan
 * @description
 * @date 2020/12/17
 */
@Slf4j
public class ConvertUtil {
    private static final IvParameterSpec SEC_IV;

    static {
        byte[] sys = DigestUtils.md5(SystemUtil.getSystemKey());
        byte[] iv = new byte[16];
        System.arraycopy(sys, 0, iv, 0, 16);
        SEC_IV = new IvParameterSpec(iv);
    }

    private ConvertUtil() {
    }

    public static String encodeBase64(byte[] data) {
        return Base64.encodeBase64String(data);
    }

    public static byte[] decodeBase64(String data) {
        return Base64.decodeBase64(data);
    }

    public static String decodeBase64Str(String data) {
        return new String(decodeBase64(data));
    }

    public static String toHex(byte[] content) {
        return Hex.encodeHexString(content);
    }

    public static byte[] fromHex(String data) throws DecoderException {
        return Hex.decodeHex(data);
    }

    public static String sha256(String data) {
        return DigestUtils.sha256Hex(data);
    }

    public static byte[] getQrCodeImageBytes(String content, int width, int height, String format) throws WriterException, IOException {
                /*
           定义二维码的参数
        */
        EnumMap<EncodeHintType, Object> option = new EnumMap<>(EncodeHintType.class);
        // 设置二维码字符编码
        option.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 设置二维码纠错等级
        option.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        // 设置二维码边距
        option.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, option);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ImageIO.write(image, format, bout);

        return bout.toByteArray();
    }

    public static Image getQrCodeImage(String content, int width, int height) throws WriterException {
        //定义二维码的参数
        EnumMap<EncodeHintType, Object> option = new EnumMap<>(EncodeHintType.class);
        // 设置二维码字符编码
        option.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 设置二维码纠错等级
        option.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        // 设置二维码边距
        option.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, option);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    public static String encrypt(String msg, String key) throws BadEncryptException {
        return encodeBase64(encrypt(msg.getBytes(), key));
    }

    public static String decrypt(String msg, String key) throws BadEncryptException {
        return new String(decrypt(decodeBase64(msg), key));
    }

    public static byte[] encrypt(byte[] content, String key) throws BadEncryptException {
        try {
            if (key.length() < 16) {
                key = (key + "0000000000000000");
            }
            key = key.substring(0, 16);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, SEC_IV);
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException e) {
            throw new BadEncryptException(e);
        } catch (InvalidKeyException e) {
            throw new BadEncryptException("密钥格式不符合要求", e);
        } catch (NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new BadEncryptException("解密失败", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new BadEncryptException("非法向量");
        }
    }

    public static byte[] decrypt(byte[] msg, String key) throws BadEncryptException {
        try {
            if (key.length() < 16) {
                key = (key + "0000000000000000");
            }
            key = key.substring(0, 16);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, SEC_IV);
            return cipher.doFinal(msg);
        } catch (NoSuchAlgorithmException e) {
            throw new BadEncryptException(e);
        } catch (InvalidKeyException e) {
            throw new BadEncryptException("密钥格式不符合要求", e);
        } catch (NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new BadEncryptException("密文格式异常", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new BadEncryptException("非法向量");
        }
    }

    private static final long KB_SIZE = 1024;
    private static final long MB_SIZE = 1024 * KB_SIZE;
    private static final long GB_SIZE = 1024 * MB_SIZE;

    public static String toShortSize(long size) {
        if (size > GB_SIZE) {
            return String.format("%.2fGB", 1f * size / GB_SIZE);
        } else if (size > MB_SIZE) {
            return String.format("%.2fMB", 1f * size / MB_SIZE);
        } else if (size > KB_SIZE) {
            return String.format("%.2fKB", 1f * size / KB_SIZE);
        } else {
            return size + "B";
        }
    }

    public static String combines(String delimiter, Object... patterns) {
        return combines(false, delimiter, patterns);
    }

    public static String combines(boolean ignoreNull, String delimiter, Object... patterns) {
        if (null == patterns) {
            return "";
        }
        StringBuilder bd = new StringBuilder();
        for (Object pat : patterns) {
            if (pat instanceof Object[]) {
                bd.append(combines(ignoreNull, delimiter, (Object[]) pat));
            } else if (null != pat) {
                bd.append(pat);
            } else if (ignoreNull) {
                continue;
            } else {
                bd.append("null");
            }
            bd.append(delimiter);
        }
        bd.deleteCharAt(bd.length() - 1);
        return bd.toString();
    }

    public static String limitStringSize(String src, int limit) {
        if (null != src && src.length() > limit) {
            int f = limit / 2;
            int e = limit - f;
            return src.substring(0, f) + "\n......\n" +
                    src.substring(src.length() - e - 1, src.length() - 1);
        }
        return src;
    }

    public static File covertDir2Zip(File src, String path, InitialResourceWrapper wrapper, Executor asyncExecutor) {
        File file = new File(path);
        if (file.isDirectory()) {
            file = new File(file, src.getName() + ".zip");
        }

        File fin = file;
        Runnable run = () -> {
            boolean failed = false;
            if (!fin.exists()) {
                try {
                    ZipUtil.nioZip(src, fin);
                } catch (IOException e) {
                    failed = true;
                    log.error("zip file failed", e);
                }
            }
            log.info("zip '{}' finished", fin);
            if (!failed && wrapper != null) {
                wrapper.complete(fin);
            }
        };
        if (null == asyncExecutor) {
            run.run();
        } else {
            asyncExecutor.execute(run);
        }

        return fin;
    }

    public static String shortName(String name, int limit) {
        int len = name.length() + limit;
        if (len > name.length() && limit < name.length()) {
            return name.substring(0, limit) + "...";
        } else if (len < name.length() && len > 0) {
            return name.substring(len) + "...";
        } else {
            return name;
        }
    }

    public static String getZipName(File file) {
        if (file.isDirectory()) {
            return file.getName() + ".zip";
        }
        return file.getName().replaceAll("(.*)(\\..*)?", "$1.zip");
    }

}
