package top.ivan.windrop.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import top.ivan.windrop.ex.BadEncryptException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.EnumMap;

/**
 * @author Ivan
 * @description
 * @date 2020/12/17
 */
public class ConvertUtil {
    private static final IvParameterSpec SEC_IV;

    static {
        SecureRandom sr = new SecureRandom(SystemUtil.getSystemKey().getBytes(StandardCharsets.UTF_8));
        byte[] iv = new byte[16];
        sr.nextBytes(iv);
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
        if (null == patterns) {
            return "";
        }
        StringBuilder bd = new StringBuilder();
        boolean deleteLast = false;
        for (Object pat : patterns) {
            if (pat instanceof Object[]) {
                bd.append(combines(delimiter, (Object[]) pat));
                deleteLast = false;
            } else if (null != pat) {
                bd.append(pat).append(delimiter);
                deleteLast = true;
            } else {
                bd.append(delimiter);
                deleteLast = false;
            }
        }
        if (deleteLast) {
            bd.deleteCharAt(bd.length() - 1);
        }
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
}
