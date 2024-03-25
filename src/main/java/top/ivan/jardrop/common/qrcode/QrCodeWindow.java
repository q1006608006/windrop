package top.ivan.jardrop.common.qrcode;

import com.google.zxing.WriterException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Ivan
 * @since 2024/03/19 10:50
 */
public class QrCodeWindow {
    private static final Function<Integer, String> DEFAULT_REST_COUNTER_TEXT_PARSER = d -> String.format("二维码剩余有效时间: %ds", d - 1);
    private final int size;

    private final int expiredSecond;
    private final Supplier<String> codeTextSupplier;
    private Function<Integer, String> restTextParser = DEFAULT_REST_COUNTER_TEXT_PARSER;
    private Consumer<QrCodeWindow> onExpire;
    private Consumer<QrCodeWindow> onClose;

    private final DisplayWindow display;

    public QrCodeWindow(String title, int size, int expiredSecond, Supplier<String> codeTextSupplier) {
        this.size = size;
        this.expiredSecond = expiredSecond;
        this.codeTextSupplier = codeTextSupplier;
        this.display = new DisplayWindow(title);
    }

    public void setQrImageText(String content) {
        Image image;
        try {
            image = QrUtils.toQrCode(content, size, size);
            this.display.setImage(image);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    private void onExpire() {
        if (null != onExpire) {
            onExpire.accept(this);
        }
    }

    private void onClose() {
        if (null != onClose) {
            onClose.accept(this);
        }
    }

    public void refresh() {
        String content = codeTextSupplier.get();
        if (null != content) {
            setQrImageText(content);
        }
        this.display.resetCounter();
    }

    public void active() {
        refresh();
        this.display.active();
    }

    public void close() {
        this.display.close();
    }

    public void setRestTextParser(Function<Integer, String> restTextParser) {
        this.restTextParser = restTextParser;
    }

    public void setDescription(String des) {
        String displayText = "<html>" + des + "</html>";
        displayText = displayText.replaceAll("\n", "<br>");
        this.display.desLabel.setText(displayText);
    }

    public void onExpire(Consumer<QrCodeWindow> onExpire) {
        this.onExpire = onExpire;
    }

    public void onClose(Consumer<QrCodeWindow> onClose) {
        this.onClose = onClose;
    }

    private class DisplayWindow extends JFrame {
        private final JLabel imageLabel;
        private final JLabel countdownLabel;
        private final JLabel desLabel;
        private Timer timer;

        private volatile int curCount;

        public DisplayWindow(String title) {

            imageLabel = new JLabel();
            countdownLabel = new JLabel();
            desLabel = new JLabel();

            setTitle(title);
            setListeners();

            initUI();
            //init timer
            initTimer();
        }

        private void initTimer() {
            if (expiredSecond < 1) {
                return;
            }
            timer = new Timer(1000, e -> {
                updateCounter(--curCount);
                if (curCount == 0) {
                    QrCodeWindow.this.onExpire();
                }
            });
            timer.setRepeats(true);
        }

        public void setDescription(String des) {
            desLabel.setText(des);
        }

        public void setImage(Image image) {
            imageLabel.setIcon(new ImageIcon(image));
        }

        public void resetCounter() {
            if (null == timer) {
                return;
            }
            timer.stop();
            timer.start();
            curCount = expiredSecond;
            updateCounter(curCount);
        }

        public void updateCounter(int curCount) {
            if (null != timer) {
                countdownLabel.setText(restTextParser.apply(curCount));
            }
        }

        public void active() {
            setVisible(true);
            if (timer != null && !timer.isRunning()) {
                resetCounter();
            }
        }

        public void close() {
            if (timer != null) {
                timer.stop();
            }
            setVisible(false);
            dispose();
        }

        private void setListeners() {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    close();
                    QrCodeWindow.this.onClose();
                }
            });

            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    QrCodeWindow.this.onExpire();
                    resetCounter();
                    super.mouseClicked(e);
                }
            });
        }

        private void initUI() {
            setSize(400, 250);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GroupLayout layout = new GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);
            layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(desLabel)
                    .addComponent(imageLabel)
                    .addComponent(countdownLabel)
            );

            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addComponent(desLabel)
                    .addComponent(imageLabel)
                    .addComponent(countdownLabel)
            );

            setLocationRelativeTo(null);

            setCenter();
        }

        private void setCenter() {
            // 获取屏幕大小
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = screenSize.width;
            int screenHeight = screenSize.height;

            // 获取窗口大小
            int windowWidth = getWidth();
            int windowHeight = getHeight();

            // 计算窗口居中时的坐标
            int x = (screenWidth - windowWidth) / 2;
            int y = (screenHeight - windowHeight) / 2;

            // 设置窗口位置
            setLocation(x, y);
        }

    }

    private static String formatShowText(String src) {
        String displayText = "<html>" + src + "</html>";
        displayText = displayText.replaceAll("\n", "<br>");
        return displayText;
    }

}
