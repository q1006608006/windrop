package top.ivan.windrop.bean;

import lombok.Data;

import java.util.List;

/**
 * @author Ivan
 * @since 2021/08/13 15:41
 */
@Data
public abstract class QrProperties {
    private String type;
    private List<String> ipList;
    private int port;
}
