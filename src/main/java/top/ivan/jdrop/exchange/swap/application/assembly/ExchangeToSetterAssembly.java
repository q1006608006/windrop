package top.ivan.jdrop.exchange.swap.application.assembly;

import top.ivan.jdrop.clipboard.userinterface.ClipboardSetterVO;
import top.ivan.jdrop.common.constant.MediaType;
import top.ivan.jdrop.common.util.Base64Utils;
import top.ivan.jdrop.exchange.common.vo.UploadData;

public class ExchangeToSetterAssembly {

    public static ClipboardSetterVO toSetter(MediaType mediaType, UploadData data) {
        return new ClipboardSetterVO(mediaType, data.getResourceName(), Base64Utils.decodeBase64(data.getContent()));
    }
}
