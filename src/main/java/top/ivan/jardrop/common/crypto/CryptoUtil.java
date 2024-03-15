package top.ivan.jardrop.common.crypto;

import org.apache.commons.codec.digest.DigestUtils;
import top.ivan.windrop.exception.BadEncryptException;
import top.ivan.windrop.system.SystemUtils;
import top.ivan.windrop.util.ConvertUtil;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Ivan
 * @since 2024/03/08 10:50
 */
public class CryptoUtil {
    private static final IvParameterSpec SEC_IV;

    static {
        byte[] sys = DigestUtils.md5(SystemUtils.getSystemKey());
        byte[] iv = new byte[16];
        System.arraycopy(sys, 0, iv, 0, 16);
        SEC_IV = new IvParameterSpec(iv);
    }

    public static String encrypt(String msg, String key) throws BadEncryptException {
        return ConvertUtil.encodeBase64(encrypt(msg.getBytes(), key));
    }

    public static String decrypt(String msg, String key) throws BadEncryptException {
        return new String(decrypt(ConvertUtil.decodeBase64(msg), key));
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
}
