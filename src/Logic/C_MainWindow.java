package Logic;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.util.Optional;

public class C_MainWindow {
    @FXML private ImageView FX_ImageView_ViewInfo;
    @FXML private ImageView FX_ImageView_Help;
    @FXML private ImageView FX_ImageView_Account;
    @FXML private ImageView FX_ImageView_Exit;
    @FXML private Button FX_Button_View;


    /**
     * Method that is called when application first loads.
     * Sets up necessary variables, functions and event handlers.
     */
    public void initialize(){
        // Sets up the images for the buttons
        SetUpImages(75, 100, true);

        // Event handler to handle view button click
        FX_Button_View.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                boolean con = B_DrawWindows.B_DrawWindows_instance.DrawNewStage(
                        "FXML_ViewInformation.fxml",
                        "View records",
                        600,
                        700
                );
            }
        });

        HandleLogIn();
    }


    /**
     * A function used to set-up images.
     * @param Height the height of the images
     * @param Width the width of the images
     * @param PreserveRatio whether to preserve the ratio of the images
     */
    public void SetUpImages(double Height, double Width, boolean PreserveRatio){

        FX_ImageView_ViewInfo.setImage(new Image("/Images/View_Info.png"));
        FX_ImageView_ViewInfo.setFitHeight(Height);
        FX_ImageView_ViewInfo.setFitWidth(Width);
        FX_ImageView_ViewInfo.setPreserveRatio(PreserveRatio);

        FX_ImageView_Help.setImage(new Image("/Images/Help.png"));
        FX_ImageView_Help.setFitHeight(Height);
        FX_ImageView_Help.setFitWidth(Width);
        FX_ImageView_Help.setPreserveRatio(PreserveRatio);

        FX_ImageView_Account.setImage(new Image("/Images/Account.png"));
        FX_ImageView_Account.setFitHeight(Height);
        FX_ImageView_Account.setFitWidth(Width);
        FX_ImageView_Account.setPreserveRatio(PreserveRatio);

        FX_ImageView_Exit.setImage(new Image("/Images/Exit.png"));
        FX_ImageView_Exit.setFitHeight(Height);
        FX_ImageView_Exit.setFitWidth(Width);
        FX_ImageView_Exit.setPreserveRatio(PreserveRatio);
    }


    /**
     * Handles log in process.
     */
    private void HandleLogIn(){
        boolean Status = false, con = false;
        C_LogInDialog c_logInDialog = null;
        Optional<ButtonType> result = null;

        while(!Status) {
            con = B_DrawWindows.B_DrawWindows_instance.DrawDialog(
                    "FXML_LogInDialog.fxml",
                    "Log in to your account",
                    "Please log in to your account to proceed",
                    null
            );

            if (con) {
                c_logInDialog = B_DrawWindows.getFxmlLoader().getController();
                result = B_DrawWindows.getDialog().showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    Status = c_logInDialog.HandleLogIn();

                    if (Status == false) {
                        B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                "Failed",
                                "Log in failed",
                                "Invalid Username and/or password",
                                "ERROR"
                        );
                    }
                }
                else if (result.isPresent() && result.get() == ButtonType.CANCEL){
                    break;
                }
            }
        }

        if (!Status){
            Platform.exit();
        }
    }
}
