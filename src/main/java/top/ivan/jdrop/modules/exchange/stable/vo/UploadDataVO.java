package top.ivan.jdrop.modules.exchange.stable.vo;

import lombok.Data;
import org.springframework.http.codec.multipart.Part;

import java.util.List;

@Data
public class UploadDataVO {
    private String ticket;
    private List<Part> resourceParts;
}
