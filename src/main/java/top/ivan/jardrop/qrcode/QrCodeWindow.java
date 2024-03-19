package top.ivan.jardrop.qrcode;

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
    private static final Function<Integer, String> DEFAULT_REST_COUNTER_TEXT_PARSER = d -> String.format("有效时间剩余: %ds", d - 1);
    private final int size;

    private final int refreshInterval;
    private final Supplier<String> codeTextSupplier;
    private Function<Integer, String> restTextParser = DEFAULT_REST_COUNTER_TEXT_PARSER;
    private Consumer<QrCodeWindow> onUpdate;
    private Consumer<QrCodeWindow> onClose;

    private final DisplayWindow display;

    public QrCodeWindow(String title, int size, int refreshInterval, Supplier<String> codeTextSupplier) {
        this.size = size;
        this.refreshInterval = refreshInterval;
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

    private void update() {
        String content = codeTextSupplier.get();
        if (null != content) {
            setQrImageText(content);
        }
        if (null != onUpdate) {
            onUpdate.accept(this);
        }
    }

    public void active() {
        update();
        this.display.active();
    }

    public void close() {
        if (null != onClose) {
            onClose.accept(this);
        }
    }

    public void setRestTextParser(Function<Integer, String> restTextParser) {
        this.restTextParser = restTextParser;
    }

    public void setDescription(String des) {
        String displayText = "<html>" + des + "</html>";
        displayText = displayText.replaceAll("\n", "<br>");
        this.display.desLabel.setText(displayText);
    }

    public void setOnUpdate(Consumer<QrCodeWindow> onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void setOnClose(Consumer<QrCodeWindow> onClose) {
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
            if (refreshInterval < 1) {
                return;
            }
            timer = new Timer(1000, e -> {
                if (--curCount == 0) {
                    QrCodeWindow.this.update();
                    resetCounter();
                } else {
                    updateCounter(curCount);
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
            timer.stop();
            timer.start();
            curCount = refreshInterval;
            updateCounter(curCount);
        }

        public void updateCounter(int curCount) {
            countdownLabel.setText(restTextParser.apply(curCount));
        }

        public void active() {
            setVisible(true);
            if (!timer.isRunning()) {
                resetCounter();
                timer.start();
            }
        }

        public void close() {
            timer.stop();
            setVisible(false);
            dispose();
        }

        private void setListeners() {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    close();
                    QrCodeWindow.this.close();
                }
            });

            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    QrCodeWindow.this.update();
                    resetCounter();
                    super.mouseClicked(e);
                }
            });
        }

        private void initUI() {
            setSize(400, 250);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

/*            setLayout(new BorderLayout());
            add(desLabel, BorderLayout.NORTH);
            add(imageLabel, BorderLayout.CENTER);
            add(countdownLabel, BorderLayout.SOUTH);*/

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


//            pack();
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

}
