package top.ivan.windrop.bean;

import lombok.Data;

/**
 * @author Ivan
 * @description 请求数据
 * @date 2021/3/5
 */
@Data
public class ApplyRequest {
    /**
     * 设备id
     */
    private String id;

    /**
     * 请求类型：pull,file,text,image
     */
    private String type;

    /**
     * 当请求类型为file时的文件名
     */
    private String filename;

    /**
     * 数据大小
     */
    private String size;

    /*
    * sha256摘要
    */
    private String summary;
}
