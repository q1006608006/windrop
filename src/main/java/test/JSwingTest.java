package test;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.stream.Stream;

/**
 * @author Ivan
 * @since 2023/06/04 14:17
 */
public class JSwingTest {

    public static class FxView extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            FXMLLoader fxmlLoader = new FXMLLoader(FxView.class.getClassLoader().getResource("winconf.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setTitle("t");
            stage.show();
        }
    }

    public static class FxController {
        @FXML
        private Label openLabel;

        @FXML
        private CheckBox cbFile;

        @FXML
        private CheckBox cbUrl;

        @FXML
        public void onApply() {
            String misk = cbFile.isSelected() ? "1" : "0";
            misk += cbUrl.isSelected() ? "1" : "0";
            openLabel.setText(misk);
        }

    }


    public static class SB {

        static {
            System.out.println("nmb");
        }

        public int a = 40;

        public SB() {
            System.out.println("sb");
        }

        {
            System.out.println(a);
        }
    }

    public static class SBEZ extends SB {

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            System.out.println("dee");
            SB = this;
        }
    }

    public static SBEZ SB;

    public static void main(String[] args) throws InterruptedException {
        SB = new SBEZ();
        SB= null;
        System.gc();
        Thread.sleep(500);
        System.out.println(SB);

        SB = null;
        System.gc();
        Thread.sleep(500);
        System.out.println(SB);

    }
}
