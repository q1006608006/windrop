package top.ivan.windrop.ex;

import org.springframework.http.HttpStatus;
import top.ivan.windrop.clip.ClipBean;

/**
 * @author Ivan
 * @description
 * @date 2021/3/29
 */
public class LengthTooLargeException extends HttpClientException {

    private ClipBean clipBean;

    public LengthTooLargeException(ClipBean bean) {
        super(HttpStatus.FORBIDDEN, "传输的数据过长");
        this.clipBean = bean;
    }


    public ClipBean getClipBean() {
        return clipBean;
    }
}
