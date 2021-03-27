package top.ivan.windrop.bean;

import lombok.Data;

/**
 * @author Ivan
 * @description
 * @date 2021/3/5
 */
@Data
public class ApplyRequest {
    private String id;
    private String type;
    private String filename;
    private String size;
}
