package top.ivan.jdrop.exchange.application.assembly;

import top.ivan.jdrop.framework.system.clipboard.userinterface.ClipboardSetterVO;
import top.ivan.jdrop.framework.constant.MediaType;
import top.ivan.jdrop.tool.Base64Utils;
import top.ivan.jdrop.exchange.userinterface.vo.upload.UploadData;

public class ExchangeToSetterAssembly {

    public ClipboardSetterVO toSetter(MediaType mediaType, UploadData data) {
        return new ClipboardSetterVO(mediaType, data.getResourceName(), Base64Utils.decodeBase64(data.getContent()));
    }
}
