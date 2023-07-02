//package test;
//
//import javafx.application.Application;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Scene;
//import javafx.scene.control.CheckBox;
//import javafx.scene.control.Label;
//import javafx.stage.Stage;
//
///**
// * @author Ivan
// * @since 2023/06/04 14:17
// */
//public class JSwingTest {
//
//    public static class FxView extends Application {
//
//        @Override
//        public void start(Stage stage) throws Exception {
//            FXMLLoader fxmlLoader = new FXMLLoader(FxView.class.getClassLoader().getResource("winconf.fxml"));
//            Scene scene = new Scene(fxmlLoader.load());
//            stage.setScene(scene);
//            stage.setTitle("t");
//            stage.show();
//        }
//    }
//
//    public static class FxController {
//        @FXML
//        private Label openLabel;
//
//        @FXML
//        private CheckBox cbFile;
//
//        @FXML
//        private CheckBox cbUrl;
//
//        @FXML
//        public void onApply() {
//            String misk = cbFile.isSelected() ? "1" : "0";
//            misk += cbUrl.isSelected() ? "1" : "0";
//            openLabel.setText(misk);
//        }
//
//    }
//
//
//    public static void main(String[] args) {
////        System.out.println(FxView.class.getClassLoader().getResource("icon.png"));
//        Application.launch(FxView.class, args);
//    }
//}
