package top.ivan.jdrop.exchange.userinterface.vo.security;

import lombok.Data;
import top.ivan.jdrop.framework.constant.MediaType;
import top.ivan.jdrop.framework.constant.OperateType;

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
