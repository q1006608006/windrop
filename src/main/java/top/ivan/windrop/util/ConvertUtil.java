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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * @author Ivan
 * @description
 * @date 2020/12/17
 */
public class ConvertUtil {

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
        HashMap<EncodeHintType, Object> hashMap = new HashMap<>();
        // 设置二维码字符编码
        hashMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 设置二维码纠错等级
        hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        // 设置二维码边距
        hashMap.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hashMap);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ImageIO.write(image, format, bout);

        return bout.toByteArray();
    }

    public static Image getQrCodeImage(String content, int width, int height, String format) throws WriterException, IOException {
        //定义二维码的参数
        HashMap<EncodeHintType, Object> hashMap = new HashMap<>();
        // 设置二维码字符编码
        hashMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 设置二维码纠错等级
        hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        // 设置二维码边距
        hashMap.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hashMap);

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
                key = (key + "0000000000000000").substring(0, 16);
            }
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(key.substring(0, 16).getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
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
                key = (key + "0000000000000000").substring(0, 16);
            }
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(key.substring(0, 16).getBytes());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
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

}
