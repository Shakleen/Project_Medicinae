package Logic;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This class is used to mainly initiate the program. It launches the main window.
 * <p>The methods of this class are start main. Start create the main window and launches it.</p>
 *
 * @author Shakleen Ishfar
 * @version 1.0.0.0
 * @since 04 November, 2018
 */
public class Driver extends Application {

    /**
     * For launching main window.
     * @param primaryStage the stage that is to be drawn
     * @throws Exception if there is a problem when drawing the window.
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("FXML_MainWindow.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1024, 615));
        primaryStage.show();
    }

    /**
     * Launches the application.
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
