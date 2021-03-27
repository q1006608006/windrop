package top.ivan.windrop.tray;

import javafx.util.Pair;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class WindropSystemTray {
    private String title;
    private List<Pair<String, MenuElement>> elements;
    private Image icon;
    private List<Pair<String, MenuItem>> menuList;
    private TrayIcon trayIcon;

    private WindropSystemTray(String title, Image icon, List<Pair<String, MenuElement>> elements) {
        if (!java.awt.SystemTray.isSupported()) {
            log.warn("it is not support system tray in this machine");
        } else {
            this.title = title;
            this.icon = icon;
            this.elements = elements;
            init();
        }
    }

    public List<MenuItem> getMenus(String title) {
        List<MenuItem> list = new ArrayList<>();
        menuList.forEach(p -> {
            if (Objects.equals(title, p.getKey())) list.add(p.getValue());
        });
        return list.isEmpty() ? null : list;
    }

    private void init() {
        SystemTray tray = SystemTray.getSystemTray();
        String notifyText = this.title;
        menuList = new ArrayList<>();

        PopupMenu popMenu = new PopupMenu();
        initMenus(popMenu, elements);

        TrayIcon trayIcon = new TrayIcon(icon, notifyText, popMenu);
        try {
            tray.add(trayIcon);
            trayIcon.setImageAutoSize(true);
            this.trayIcon = trayIcon;
        } catch (AWTException e1) {
            e1.printStackTrace();
        }

    }

    private void initMenus(Menu menu, List<Pair<String, MenuElement>> elements) {
        for (Pair<String, MenuElement> pair : elements) {
            if (pair.getKey() == null) {
                menu.addSeparator();
            } else {
                MenuElement ele = pair.getValue();
                if (ele instanceof MenuLabel) {
                    MenuLabel ml = (MenuLabel) ele;
                    MenuItem itmOpen = new MenuItem(pair.getKey());
                    itmOpen.addActionListener(t -> ml.onAction(itmOpen, t));
                    menu.add(itmOpen);
                    menuList.add(new Pair<>(pair.getKey(), itmOpen));
                } else if (ele instanceof MenuDrawer) {
                    Menu drawerMenu = new Menu(pair.getKey());
                    initMenus(drawerMenu, ((MenuDrawer) ele).getElements());
                    menu.add(drawerMenu);
                }
            }
        }
    }

    public void showNotification(String message) {
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
    }

    public void showConfirm(String message) {

    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

    public static class Builder {
        private String title;
        private List<Pair<String, MenuElement>> elements;
        private Image icon;

        public Builder showText(String text) {
            this.title = text;
            return this;
        }

        public Builder addLabel(String title, MenuLabel label) {
            getElements().add(new Pair<>(title, label));
            return this;
        }

        public Builder addSecondLabel(String drawer, String title, MenuLabel label) {
            List<MenuElement> menus = getElements(drawer);
            MenuDrawer md;
            if (menus == null || !(menus.get(0) instanceof MenuDrawer)) {
                md = new MenuDrawer();
                getElements().add(new Pair<>(drawer, md));
            } else {
                md = (MenuDrawer) menus.get(0);
            }
            md.getElements().add(new Pair<>(title, label));
            return this;
        }

        public Builder addMenu(String drawer, MenuDrawer md) {
            if (null != md) {
                getElements().add(new Pair<>(drawer, md));
            }
            return this;
        }

        public Builder addSeparator() {
            getElements().add(new Pair<>(null, null));
            return this;
        }

        public MenuElement removeElement(String title) {
            for (Pair<String, MenuElement> pair : getElements()) {
                if (Objects.equals(title, pair.getKey())) {
                    return pair.getValue();
                }
            }
            return null;
        }

        public void removeElements(String title, MenuElement action) {
            List<Pair<String, MenuElement>> list = new ArrayList<>();
            for (Pair<String, MenuElement> pair : getElements()) {
                if (Objects.equals(title, pair.getKey()) && Objects.equals(action, pair.getValue())) {
                    list.add(pair);
                }
            }
            getElements().removeAll(list);
        }

        public List<MenuElement> getElements(String title) {
            List<MenuElement> list = new ArrayList<>();
            for (Pair<String, MenuElement> pair : getElements()) {
                if (Objects.equals(title, pair.getKey())) {
                    list.add(pair.getValue());
                }
            }

            return list.isEmpty() ? null : list;
        }

        public Builder icon(Image icon) {
            this.icon = icon;
            return this;
        }

        /**
         * 获取elements列表
         * 注意：非线程安全
         *
         * @return
         */
        private List<Pair<String, MenuElement>> getElements() {
            if (null == elements) {
                elements = new ArrayList<>();
            }
            return elements;
        }

        public WindropSystemTray build() {
            return new WindropSystemTray(title, icon, getElements());
        }
    }

    public interface MenuElement {
    }

    public interface MenuLabel extends MenuElement {
        void onAction(MenuItem item, ActionEvent ae);
    }

    @Data
    public static class MenuDrawer implements MenuElement {
        private List<Pair<String, MenuElement>> elements;

        public MenuDrawer(List<Pair<String, MenuElement>> labels) {
            if (labels == null) {
                labels = new ArrayList<>();
            }
            this.elements = labels;
        }

        public MenuDrawer() {
            this(null);
        }
    }

}