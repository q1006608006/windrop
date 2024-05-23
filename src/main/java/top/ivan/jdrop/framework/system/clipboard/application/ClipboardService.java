package top.ivan.jdrop.framework.system.clipboard.application;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.system.clipboard.userinterface.ClipboardSetterVO;

@Service
public class ClipboardService {

    public Mono<Void> setClipboard(ClipboardSetterVO setter) {
        return Mono.empty().then();//todo
    }
}
