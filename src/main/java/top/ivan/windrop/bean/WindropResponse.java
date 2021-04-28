package top.ivan.windrop.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Ivan
 * @description 同步数据信息
 * @date 2021/1/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WindropResponse extends CommonResponse {
    /**
     * 服务端更新时间
     */
    private long serverUpdateTime;

    /**
     * 数据类型：file，text，image
     */
    private String type;

    /**
     * 数据内容,使用base64编码
     */
    private String data;

    /**
     * 文件数据的文件名
     */
    private String filename;

    /**
     * 数据签名（sha256）
     */
    private String sign;

    /**
     * 大文件资源ID
     */
    private String resourceId;

    public WindropResponse() {
        setSuccess(true);
    }

}
