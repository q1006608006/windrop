package top.ivan.windrop.svc;

import lombok.extern.slf4j.Slf4j;
import top.ivan.windrop.system.clipboard.ClipBean;

/**
 * @author Ivan
 * @since 2023/08/25 20:28
 */
@Slf4j
public class SecureClipBean {
    private final ClipBean bean;
    private final SecurityManager manager;


    public SecureClipBean(ClipBean bean, SecurityManager manager) {
        this.bean = bean;
        this.manager = manager;
    }


}
