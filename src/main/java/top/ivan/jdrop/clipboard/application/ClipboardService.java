package top.ivan.jdrop.clipboard.application;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.clipboard.userinterface.ClipboardSetterVO;
import top.ivan.jdrop.common.constant.MediaType;

@Service
public class ClipboardService {

    public Mono<Void> setClipboard(ClipboardSetterVO setter) {
        return Mono.empty();//todo
    }
}
