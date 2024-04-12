package top.ivan.jdrop.exchange.security.userinterface.vo;

import lombok.Data;
import top.ivan.jdrop.common.constant.MediaType;
import top.ivan.jdrop.common.constant.OperateType;

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
