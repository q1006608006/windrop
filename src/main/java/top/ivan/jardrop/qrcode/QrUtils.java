package top.ivan.jardrop.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;

/**
 * @author Ivan
 * @since 2024/02/29 14:41
 */
public class QrUtils {


    public static byte[] toQrCode(String content, int width, int height, String format) throws WriterException, IOException {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ImageIO.write(toQrCode(content, width, height), format, bout);

        return bout.toByteArray();
    }

    public static BufferedImage toQrCode(String content, int width, int height) throws WriterException {
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
}
