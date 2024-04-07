package top.ivan.jdrop.security.userinterface.model.vo;

import lombok.Data;
import top.ivan.jdrop.exchange.userinterface.model.constant.MediaType;
import top.ivan.jdrop.exchange.userinterface.model.constant.OperateType;

/**
 * @author Ivan
 * @since 2024/03/29 00:24
 */
@Data
public class ApplyVO {
    private String userId;
    private OperateType operate;
    private MediaType mediaType;
}
