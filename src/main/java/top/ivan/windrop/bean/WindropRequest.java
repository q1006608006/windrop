package top.ivan.windrop.bean;

import lombok.Data;

/**
 * @author Ivan
 * @description 推送数据
 * @date 2021/1/14
 */
@Data
public class WindropRequest {
    /**
     * 设备id
     */
    private String id;

    /**
     * 推送的文件名称
     */
    private String filename;

    /**
     * 推送的文件后缀名称
     */
    private String fileSuffix;

    /**
     * 推送的数据，使用base64编码
     */
    private String data;

    /**
     * 推送数据类型
     */
    private String type;

    /**
     * 资源更新时间
     */
    private Long clientUpdateTime;

    /**
     * 数据签名（sha256）
     */
    private String sign;
}
