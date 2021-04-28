package top.ivan.windrop;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Hooks;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.svc.LocalQRConnectHandler;
import top.ivan.windrop.svc.LocalQRFileSharedHandler;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.tray.WindropSystemTray;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author 10066
 */
@Slf4j
@SpringBootApplication
public class WinDropApplication {

    public static void main(String[] args) {
        Hooks.onOperatorDebug();
        SpringApplicationBuilder builder = new SpringApplicationBuilder(WinDropApplication.class);
        builder.headless(false);
        WindropHandler handler = new WindropHandler(args, builder.build(args));
        handler.start();
    }

    public static class WindropHandler {

        public static final String ICON_START = "启动";
        public static final String ICON_STOP = "关闭";
        public static final String ICON_RESTART = "重启";

        public static final String ICON_SHOW_CODE = "连接码";
        public static final String ICON_MENU_CONFIG = "配置";
        public static final String ICON_SHOW_CONFIG = "打开配置文件";
        public static final String ICON_SHOW_ACCESSIBLE = "白名单";
        public static final String ICON_REMOVE_DEVICES = "重置认证设备";

        public static final String ICON_SHOW_LOG = "查看日志";

        public static final String ICON_SHARE_FILE = "共享文件";

        public static final String ICON_SHUTDOWN = "退出";


        private final String[] args;
        private final SpringApplication application;
        private ConfigurableApplicationContext context;

        private static Image iconImage;
        private static WindropSystemTray systemTray;
        private static Frame unVisibleFrame;

        /*
         * spring components
         * */
        private AppBeanHandler beanHandler;

        @Component
        public static class AppBeanHandler {

            @Autowired
            private WindropConfig config;
            @Autowired
            private PersistUserService userService;
            @Autowired
            private LocalQRConnectHandler connectHandler;
            @Autowired
            private LocalQRFileSharedHandler fileSharedService;
        }

        public WindropHandler(String[] args, SpringApplication application) {
            this.args = args;
            this.application = application;
            try {
                iconImage = Toolkit.getDefaultToolkit().getImage(new ClassPathResource("icon.png").getURL());
            } catch (IOException e) {
                log.warn("initialize tray icon failed", e);
            }
            createFrame();
            createTray();
        }

        public ConfigurableApplicationContext start() {
            log.info("start windrop service...");
            disableIcon(ICON_START);
            this.context = application.run(args);
            enableIcon(ICON_STOP);
            autoWired();
            log.info("windrop started.");
            return context;
        }

        private void autoWired() {
            this.beanHandler = this.context.getBean(AppBeanHandler.class);
        }

        public void stop() {
            disableIcon(ICON_STOP);
            if (null != this.context) {
                this.context.close();
            }
            enableIcon(ICON_START);
            log.info("close windrop service...");

        }

        public void restart() {
            log.info("restart windrop service...");
            disableIcon(ICON_START);
            disableIcon(ICON_STOP);
            stop();
            start();
            enableIcon(ICON_STOP);
            log.info("restart finished");
        }

        private void exit() {
            log.info("shutdown windrop now...");
            stop();
            System.exit(0);
        }

        private String getURLPrefix() {
            return "http://localhost:" + beanHandler.config.getPort();
        }

        private void showConnectCode(int second) {
            try {
                String key = beanHandler.connectHandler.newConnect(second);
                Desktop.getDesktop().browse(new URI(getURLPrefix() + "/windrop/code/" + key));
            } catch (Exception e) {
                alert("无法打开网页");
                log.error("open browser failed", e);
            }
        }


        private void showConfig() {
            File config = new File("conf/config.properties");
            try {
                if (!config.exists()) {
                    alert("未创建'conf/config.properties'文件");
                    log.warn("not found 'conf/config.properties'");
                } else {
                    Desktop.getDesktop().open(config);
                }
            } catch (Exception e) {
                alert("无法打开配置文件");
                log.error("open file '" + config.getAbsolutePath() + "' failed", e);
            }
        }

        private void showLog() {
            String logPath = "logs/server.log";
            File file = new File(logPath);
            try {
                if (!file.exists()) {
                    alert("未找到'logs/server.log'文件");
                    log.warn("can't found 'logs/server.log'");
                } else {
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception e) {
                alert("无法打开日志文件");
                log.error("open file 'logs/server.log' failed", e);
            }
        }

        private void showAccessible() {
            File file = new File("conf/ipList.txt");
            if (!file.exists()) {
                try {
                    File dir = file.getParentFile();
                    if (!dir.exists() || !dir.isDirectory()) {
                        dir.mkdirs();
                    }
                    file.createNewFile();
                } catch (Exception e) {
                    alert("无法创建白名单文件");
                    log.error("create file 'conf/ipList.txt' failed", e);
                }
            }
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception e) {
                alert("无法打开白名单文件");
                log.error("open file 'conf/ipList.txt' failed", e);
            }
        }

        private static File HOME_FILE = FileSystemView.getFileSystemView().getHomeDirectory();

        private void prepareDownload() {
            JFileChooser fileChooser = new JFileChooser();

            fileChooser.setCurrentDirectory(HOME_FILE);
            fileChooser.setDialogTitle("请选择要上传的文件...");
            fileChooser.setApproveButtonText("确定");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            File selectorFile;
            if (fileChooser.showOpenDialog(unVisibleFrame) == 0) {
                selectorFile = fileChooser.getSelectedFile();
                HOME_FILE = selectorFile.getParentFile();
            } else {
                return;
            }
            String qrKey = beanHandler.fileSharedService.sharedFile(selectorFile, 5, 300);
            try {
                Desktop.getDesktop().browse(new URI(getURLPrefix() + "/windrop/code/" + qrKey));
            } catch (URISyntaxException | IOException e) {
                alert("无法打开网页");
                log.error("open browser failed", e);
            }
        }

        private void clearDevice() {
            try {
                beanHandler.userService.deleteAll();
            } catch (IOException e) {
                alert("重置认证设备异常");
                log.error("reset devices failed", e);
            }
        }

        private void disableIcon(String title) {
            getSystemTray().getMenus(title).get(0).setEnabled(false);
        }

        private void enableIcon(String title) {
            getSystemTray().getMenus(title).get(0).setEnabled(true);
        }

        private void createTray() {
            WindropSystemTray.Builder builder = new WindropSystemTray.Builder();
            builder.showText("WinDrop").icon(iconImage)
                    .addLabel(ICON_START, (m, t) -> start())
                    .addLabel(ICON_STOP, (m, t) -> stop())
                    .addLabel(ICON_RESTART, (m, t) -> restart())
                    .addSeparator()
                    .addSecondLabel(ICON_SHOW_CODE, "10分钟", (m, t) -> showConnectCode(10 * 60))
                    .addSecondLabel(ICON_SHOW_CODE, "1小时", (m, t) -> showConnectCode(60 * 60))
                    .addSecondLabel(ICON_SHOW_CODE, "3小时", (m, t) -> showConnectCode(180 * 60))
                    .addSecondLabel(ICON_SHOW_CODE, null, null)
                    .addSecondLabel(ICON_SHOW_CODE, "1天", (m, t) -> showConnectCode(60 * 60 * 24))
                    .addSecondLabel(ICON_SHOW_CODE, "1周", (m, t) -> showConnectCode(60 * 60 * 24 * 7))
                    .addSecondLabel(ICON_SHOW_CODE, "1月", (m, t) -> showConnectCode(60 * 60 * 24 * 30))
                    .addSecondLabel(ICON_SHOW_CODE, null, null)
                    .addSecondLabel(ICON_SHOW_CODE, "永久", (m, t) -> showConnectCode(-1))
                    .addSecondLabel(ICON_MENU_CONFIG, ICON_SHOW_CONFIG, (m, t) -> showConfig())
                    .addSecondLabel(ICON_MENU_CONFIG, ICON_SHOW_ACCESSIBLE, (m, t) -> showAccessible())
                    .addSecondLabel(ICON_MENU_CONFIG, ICON_REMOVE_DEVICES, (m, t) -> clearDevice())
                    .addLabel(ICON_SHOW_LOG, (m, t) -> showLog())
                    .addSeparator()
                    .addLabel(ICON_SHARE_FILE, (m, t) -> prepareDownload())
                    .addSeparator()
                    .addLabel(ICON_SHUTDOWN, (m, t) -> exit());

            WindropHandler.systemTray = builder.build();
        }

        private void createFrame() {
            try {
                UIManager.setLookAndFeel(new WindowsLookAndFeel());
            } catch (Exception e) {
                log.warn("set UI-style failed", e);
            }
            unVisibleFrame = new Frame();
            unVisibleFrame.setIconImage(iconImage);
            unVisibleFrame.setVisible(false);
            unVisibleFrame.setAlwaysOnTop(true);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = (int) screenSize.getWidth();
            unVisibleFrame.setLocation(screenWidth - 250, 50);
        }

        public static WindropSystemTray getSystemTray() {
            if (null == systemTray) {
                throw new IllegalStateException("尚未初始化完成");
            }
            return systemTray;
        }

        public static void alert(String msg) {
            JOptionPane.showConfirmDialog(null, msg, "警告", JOptionPane.DEFAULT_OPTION);
        }

        public static boolean confirm(String title, String msg) {
            if (StringUtils.isEmpty(title)) {
                title = "请选择";
            }
            unVisibleFrame.setVisible(true);
            int showConfirmDialog = JOptionPane.showConfirmDialog(unVisibleFrame, msg, title, JOptionPane.YES_NO_OPTION);
            unVisibleFrame.setVisible(false);
            return showConfirmDialog == 0;
        }

    }

}
