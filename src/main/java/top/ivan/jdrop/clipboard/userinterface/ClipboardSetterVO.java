package top.ivan.jdrop.clipboard.userinterface;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.ivan.jdrop.common.constant.MediaType;

@Data
@AllArgsConstructor
public class ClipboardSetterVO {
    private MediaType mediaType;
    private String resourceName;
    private byte[] content;
}
