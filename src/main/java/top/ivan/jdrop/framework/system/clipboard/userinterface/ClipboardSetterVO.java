package top.ivan.jdrop.framework.system.clipboard.userinterface;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.ivan.jdrop.framework.constant.MediaType;

@Data
@AllArgsConstructor
public class ClipboardSetterVO {
    private MediaType mediaType;
    private String resourceName;
    private byte[] content;
}
