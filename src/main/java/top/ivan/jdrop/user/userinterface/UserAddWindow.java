package top.ivan.jdrop.user.userinterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.tool.cache.VolatileHandler;
import top.ivan.jdrop.framework.QrCodeWindow;
import top.ivan.jdrop.tool.JSONUtils;
import top.ivan.jdrop.user.application.UserManager;
import top.ivan.jdrop.user.application.dto.UserConnectDTO;
import top.ivan.jdrop.user.infrastructure.DateUtils;

/**
 * @author Ivan
 * @since 2024/03/29 00:54
 */
@Component
public class UserAddWindow {

    @Autowired
    private UserManager userManager;

    private static final VolatileHandler<QrCodeWindow> UNI_QR_CODE_WINDOW = new VolatileHandler<>();
    private static final VolatileHandler<UserConnectDTO> UNI_USER_CONNECT_DATA = new VolatileHandler<>();


    public void newConnectWindow(int expireMillions) {
        //init window
        QrCodeWindow window = createConnectQrCodeWindow(expireMillions);

        //set trigger for remove old bind info
        setWindowTrigger(window);

        //active
        activeWindow(expireMillions, window);
    }

    private void activeWindow(int expireMillions, QrCodeWindow window) {
        window.active();
        UserConnectDTO uc = UNI_USER_CONNECT_DATA.getValue();
        window.setDescription(formatQrCodeDescription(expireMillions, uc.getHostName(), uc.getNetPort()));
    }

    private void setWindowTrigger(QrCodeWindow window) {
        window.onExpire(w -> userManager.removeConnectBind(UNI_USER_CONNECT_DATA.getValue())
                .then(Mono.fromRunnable(w::refresh))
                .toFuture().join()
        );
        window.onClose(w -> userManager.removeConnectBind(UNI_USER_CONNECT_DATA.getValue())
                .then(Mono.fromRunnable(() -> UNI_USER_CONNECT_DATA.setValue(null)))
                .then(Mono.fromRunnable(() -> UNI_QR_CODE_WINDOW.setValue(null)))
                .toFuture().join()
        );
    }

    private QrCodeWindow createConnectQrCodeWindow(int expireMillions) {
        //release last window
        if (UNI_QR_CODE_WINDOW.hasValue()) {
            UNI_QR_CODE_WINDOW.getValue().close();
        }

        //create new window
        QrCodeWindow window = new QrCodeWindow("连接码", 110, 60,
                () -> userManager.newUserConnect(expireMillions)
                        .doOnNext(UNI_USER_CONNECT_DATA::setValue)
                        .map(JSONUtils::toString)
                        .toFuture().join()
        );

        //set unique qr_code_window
        UNI_QR_CODE_WINDOW.setValue(window);
        return window;
    }


    private static String formatQrCodeDescription(int expireMillions, String hostname, int serverPort) {
        return String.format("连接域名: %s\n" +
                        "服务端口: %d\n" +
                        "连接有效期: %s",
                hostname, serverPort, DateUtils.secondToDateDuration(expireMillions / 1000));
    }

}
