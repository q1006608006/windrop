package top.ivan.windrop;

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
import top.ivan.windrop.shortcut.ShortcutApiManager;
import top.ivan.windrop.svc.*;
import top.ivan.windrop.tray.WindropSystemTray;
import top.ivan.windrop.util.SystemUtil;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 10066
 */
@Slf4j
@SpringBootApplication
public class WinDropApplication {
    private static WindropHandler handlerInstant;

    public static void main(String[] args) {
        Hooks.onOperatorDebug();
        SpringApplicationBuilder builder = new SpringApplicationBuilder(WinDropApplication.class);
        builder.headless(false);
        WindropHandler handler = new WindropHandler(args, builder.build(args));
        handlerInstant = handler;
        handler.startApp();
    }

    public static void alert(String msg) {
        JOptionPane.showConfirmDialog(null, msg, "警告", JOptionPane.DEFAULT_OPTION);
    }

    public static boolean confirm(String title, String msg) {
        if (!StringUtils.hasLength(title)) {
            title = "请选择";
        }
        handlerInstant.unVisibleFrame.setVisible(true);
        int showConfirmDialog = JOptionPane.showConfirmDialog(handlerInstant.unVisibleFrame, msg, title, JOptionPane.YES_NO_OPTION);
        handlerInstant.unVisibleFrame.setVisible(false);
        return showConfirmDialog == 0;
    }

    public static void showNotification(String msg) {
        handlerInstant.systemTray.showNotification(msg);
    }

    public static void open(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            log.error("open \"" + file + "\" failed", e);
        }
    }

    public static class WindropHandler {

        public static final String ICON_START = "启动";
        public static final String ICON_STOP = "关闭";
        public static final String ICON_RESTART = "重启";

        public static final String ICON_SHOW_CODE = "连接码";
        public static final String ICON_SHOW_SHORTCUT = "快捷指令";
        public static final String ICON_MENU_CONFIG = "配置";
        public static final String ICON_SHOW_CONFIG = "打开配置文件";
        public static final String ICON_SHOW_ACCESSIBLE = "白名单";
        public static final String ICON_REMOVE_DEVICES = "清空设备";

        public static final String ICON_SHOW_LOG = "查看日志";

        public static final String ICON_SHARE_FILE = "共享文件";
        public static final String ICON_RECV_FILE = "已收文件";

        public static final String ICON_SHUTDOWN = "退出";


        private final String[] args;
        private final SpringApplication application;
        private ConfigurableApplicationContext context;

        private Image iconImage;
        private WindropSystemTray systemTray;
        private Frame unVisibleFrame;
        private File homeFile = FileSystemView.getFileSystemView().getHomeDirectory();

        /*
         * spring components
         * */
        private AppBeanHandler beanHandler;

        @Component
        public static class AppBeanHandler {

            private WindropConfig config;
            @Autowired
            private PersistUserService userService;
            @Autowired
            private LocalQRConnectHandler connectHandler;
            @Autowired
            private LocalQRFileSharedHandler fileSharedService;
            @Autowired
            private ShortcutApiManager apiManager;
            @Autowired
            private LocalQRTextHandler textHandler;

            @Autowired
            public void setConfig(WindropConfig config) {
                this.config = config;
                List<String> netList = config.getNetworkInterfaces();
                if (null == netList) {
                    netList = Collections.emptyList();
                }
                System.getProperties().put("windrop.networkInterfaces", netList);
            }
        }

        private WindropHandler(String[] args, SpringApplication application) {
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

        void autoWired() {
            this.beanHandler = this.context.getBean(AppBeanHandler.class);
        }

        void startApp() {
            log.info("start windrop service...");
            disableIcon(ICON_START);
            this.context = application.run(args);
            enableIcon(ICON_STOP);
            autoWired();
            log.info("windrop started.");
        }

        void stopApp() {
            disableIcon(ICON_STOP);
            if (null != this.context) {
                this.context.close();
            }
            enableIcon(ICON_START);
            log.info("close windrop service...");
        }

        void restartApp() {
            log.info("restart windrop service...");
            disableIcon(ICON_START);
            disableIcon(ICON_STOP);
            stopApp();
            startApp();
            enableIcon(ICON_STOP);
            log.info("restart finished");
        }

        void exit() {
            log.info("shutdown windrop now...");
            stopApp();
            System.exit(0);
        }

        private String getURLPrefix() {
            return "http://localhost:" + beanHandler.config.getPort();
        }

        private void showConnectCode(int second) {
            String key = beanHandler.connectHandler.newConnect(second);
            openQrCodeOnBrowse(key);
        }

        private void showText(String text) {
            openQrCodeOnBrowse(beanHandler.textHandler.shareText(text));
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
                    Desktop.getDesktop().open(file.getParentFile());
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
                    Files.write(file.toPath(), SystemUtil.getLocalIPList().stream().filter(s -> !"127.0.0.1".equals(s)).map(s -> s.replaceAll("(.*)\\.\\d{1,3}$", "$1.1>255")).collect(Collectors.toList()), StandardOpenOption.CREATE);
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

        private void prepareDownload() {
            JFileChooser fileChooser = new JFileChooser();

            fileChooser.setCurrentDirectory(homeFile);
            fileChooser.setDialogTitle("请选择要上传的文件...");
            fileChooser.setApproveButtonText("确定");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            File selectorFile;
            if (fileChooser.showOpenDialog(unVisibleFrame) == 0) {
                selectorFile = fileChooser.getSelectedFile();
                homeFile = selectorFile.getParentFile();
            } else {
                return;
            }
            String qrKey = beanHandler.fileSharedService.sharedFile(selectorFile, 5, 300);
            openQrCodeOnBrowse(qrKey);
        }

        private void openReceivedFolder() {
            try {
                Desktop.getDesktop().open(new File("recv"));
            } catch (IOException e) {
                alert("无法打开文件夹");
                log.error("open folder failed", e);
            }
        }

        private void clearDevice() {
            try {
                beanHandler.userService.deleteAll();
                alert("设备重置完成");
            } catch (IOException e) {
                alert("无法重置设备");
                log.error("reset devices failed", e);
            }
        }

        private void disableIcon(String title) {
            if (systemTray != null) {
                List<MenuItem> items = systemTray.getMenus(title);
                for (MenuItem item : items) {
                    item.setEnabled(false);
                }
            }
        }

        private void enableIcon(String title) {
            if (systemTray != null) {
                List<MenuItem> items = systemTray.getMenus(title);
                for (MenuItem item : items) {
                    item.setEnabled(true);
                }
            }
        }

        private void openQrCodeOnBrowse(String qrCodeKey) {
            try {
                Desktop.getDesktop().browse(new URI(LocalQRHandler.getUrlPath(getURLPrefix(), qrCodeKey)));
            } catch (Exception e) {
                alert("无法打开浏览器");
                log.error("open browser failed", e);
            }
        }

        private void createTray() {
            WindropSystemTray.Builder builder = new WindropSystemTray.Builder();
            builder.showText("WinDrop").icon(iconImage)
                    .addLabel(ICON_START, (m, t) -> startApp())
                    .addLabel(ICON_STOP, (m, t) -> stopApp())
                    .addLabel(ICON_RESTART, (m, t) -> restartApp())
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
                    .addSecondLabel(ICON_SHOW_SHORTCUT, "分享", (m, t) -> showText(beanHandler.apiManager.getShare()))
                    .addSecondLabel(ICON_SHOW_SHORTCUT, "同步", (m, t) -> showText(beanHandler.apiManager.getSync()))
                    .addSecondLabel(ICON_SHOW_SHORTCUT, "扫描二维码", (m, t) -> showText(beanHandler.apiManager.getScan()))
                    .addSecondLabel(ICON_SHOW_SHORTCUT, "上传", (m, t) -> showText(beanHandler.apiManager.getUpload()))
                    .addSecondLabel(ICON_MENU_CONFIG, ICON_SHOW_CONFIG, (m, t) -> showConfig())
                    .addSecondLabel(ICON_MENU_CONFIG, ICON_SHOW_ACCESSIBLE, (m, t) -> showAccessible())
                    .addSecondLabel(ICON_MENU_CONFIG, ICON_REMOVE_DEVICES, (m, t) -> clearDevice())
                    .addLabel(ICON_SHOW_LOG, (m, t) -> showLog())
                    .addSeparator()
                    .addLabel(ICON_SHARE_FILE, (m, t) -> prepareDownload())
                    .addLabel(ICON_RECV_FILE, (m, t) -> openReceivedFolder())
                    .addSeparator()
                    .addLabel(ICON_SHUTDOWN, (m, t) -> exit());

            systemTray = builder.build();
        }

        private void createFrame() {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable e) {
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

    }

}
