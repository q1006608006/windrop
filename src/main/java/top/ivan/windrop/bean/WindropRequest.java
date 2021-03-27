package top.ivan.windrop.bean;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;

/**
 * @author Ivan
 * @description
 * @date 2021/1/14
 */
@Data
@JSONType(orders = {""})
public class WindropRequest {
    private String id;
    private String fileName;
    private String fileSuffix;
    private String data;
    private String type;
    private Long clientUpdateTime;
    private String sign;
}
