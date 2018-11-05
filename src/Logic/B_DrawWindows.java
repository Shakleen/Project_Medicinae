package Logic;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.util.Optional;

/**
 * <h> Class for drawing windows such as dialogs and pop-ups </h>
 * The Driver purpose of this method is to draw windows. Meaning, it creates windows with necessary
 * information whenever necessary. It is a class that can have only one B_DrawWindows_instance as it has a private
 * constructor.
 * <p> Generally it creates dialogs and alert pop-ups </p>
 * @author Shakleen Ishfar
 * @version 1.0.0.0
 * @since 05/11/2018
 */
public class B_DrawWindows {
    public static B_DrawWindows B_DrawWindows_instance = new B_DrawWindows();
    public static Stage TeacherStage;
    public static FXMLLoader TeacherFXMLLoader;
    public static Parent TeacherRoot;
    public static Stage AdminStage;
    public static FXMLLoader AdminFXMLLoader;
    public static Parent AdminRoot;
    public static Callback<ListView<String>, ListCell<String>> cellColor;
    public static Background ListViewBackground;
    private static javafx.scene.control.Dialog<ButtonType> Dialog;
    private static  FXMLLoader fxmlLoader;
    private static  DialogPane dialogPane;
    private static  Background DialogBackground;
    private static  String DialogStyleCSS;
    private static  String FontName;
    private static  Alert alert;
    private static  Optional<ButtonType> AlertResult;

    private B_DrawWindows(){
        ListViewBackground = new Background(
                new BackgroundFill(
                        Color.color(0.25,0.25,0.25,1),
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )
        );
        cellColor = new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                ListCell <String> cell = new ListCell<String>(){
                    @Override
                    protected void updateItem(String std, boolean empty) {
                        super.updateItem(std, empty);
                        setFont(Font.font("Arial", 16));
                        setTextFill(Color.color(0, 1, 0.86, 1));

                        if (empty){
                            setText("- - -");
                        } else {
                            setText(std);
                        }
                    }
                };

                // Setting cell background color
                cell.backgroundProperty().setValue(ListViewBackground);

                return cell;
            }
        };
        FontName = "Arial";
        DialogStyleCSS = "dialog.css";
        DialogBackground = new Background(
                new BackgroundFill(
                        Color.GRAY,
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )
        );

        try {
            TeacherStage = new Stage();
            TeacherFXMLLoader = new FXMLLoader();
            TeacherFXMLLoader.setLocation(getClass().getResource("TeacherMainWindow.fxml"));
            TeacherRoot = TeacherFXMLLoader.load();
            TeacherStage.setTitle("Attendance System - Teacher");
            TeacherStage.setScene(new Scene(TeacherRoot, 1280, 720));

            AdminStage = new Stage();
            AdminFXMLLoader = new FXMLLoader();
            AdminFXMLLoader.setLocation(getClass().getResource("AdminMainWindow.fxml"));
            AdminRoot = AdminFXMLLoader.load();
            AdminStage.setTitle("Attendance System - Admin");
            AdminStage.setScene(new Scene(AdminRoot, 1280, 720));
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Create the log in dialog.
     * @param DialogName Name of the dialog.
     * @param TitleText Title of the dialog.
     * @param HeaderText Header text of the dialog.
     * @param FX_BorderPane The border pane where to get scene from
     * @return true if successfully created. False otherwise.
     */
    public boolean DrawDialog(String DialogName, String TitleText, String HeaderText, BorderPane FX_BorderPane){
        try{
            Dialog = new Dialog<>();
            fxmlLoader = new FXMLLoader();
            if (FX_BorderPane != null)
                Dialog.initOwner(FX_BorderPane.getScene().getWindow());
            Dialog.setTitle(TitleText);
            Dialog.setHeaderText(HeaderText);
            dialogPane = Dialog.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource(DialogStyleCSS).toExternalForm());
            fxmlLoader.setLocation(getClass().getResource(DialogName));
            Dialog.getDialogPane().setContent(fxmlLoader.load());
            Dialog.getDialogPane().setBackground(DialogBackground);
            Dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            Dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        } catch(IOException e){
            System.out.println("Couldn't load dialogue " + DialogName);
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Displays an error message when trying to use the software without logging in.
     * @param TitleText The title of the warning
     * @param HeaderText the header of the warning
     * @param ContentText the context of the warning
     * @param Type Type of issue generating the command.
     */
    public void DrawAlert(String TitleText, String HeaderText, String ContentText, String Type){
        String type = Type.toUpperCase();

        if (type.equals("ERROR"))                   alert = new Alert(Alert.AlertType.ERROR);
        else if (type.equals("CONFIRMATION"))       alert = new Alert(Alert.AlertType.CONFIRMATION);
        else if (type.equals("WARNING"))            alert = new Alert(Alert.AlertType.WARNING);
        else if (type.equals("INFORMATION"))        alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(TitleText);
        alert.setHeaderText(HeaderText);
        alert.setContentText(ContentText);
        dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(DialogStyleCSS).toExternalForm());
        AlertResult = alert.showAndWait();
    }

    public void DrawTeacherMainStage(){
        TeacherStage.show();
    }

    public void DrawAdminMainStage(){
        AdminStage.show();
    }

    public static javafx.scene.control.Dialog<ButtonType> getDialog() {
        return Dialog;
    }

    public static FXMLLoader getFxmlLoader() {
        return fxmlLoader;
    }

    public static DialogPane getDialogPane() {
        return dialogPane;
    }

    public static Background getDialogBackground() {
        return DialogBackground;
    }

    public static String getDialogStyleCSS() {
        return DialogStyleCSS;
    }

    public static String getFontName() {
        return FontName;
    }

    public static Alert getAlert() {
        return alert;
    }

    public static Optional<ButtonType> getAlertResult() {
        return AlertResult;
    }
}

