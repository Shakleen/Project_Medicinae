package Logic;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class C_LogInDialog {
    @FXML private TextField FX_TextField_UserName;
    @FXML private PasswordField FX_PasswordField;

    public void initialize(){
        FX_TextField_UserName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FX_TextField_UserName.selectAll();
            }
        });
        FX_PasswordField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FX_PasswordField.selectAll();
            }
        });
    }

    /**
     * Method for handling log in to dbms account.
     * @return true if successful, false otherwise.
     */
    @FXML public boolean HandleLogIn(){
        System.out.println("USER NAME = " + FX_TextField_UserName.getText());
        System.out.println("PASSWORD = " + FX_PasswordField.getText());
        return B_Database.LogIn(FX_TextField_UserName.getText(), FX_PasswordField.getText());
    }
}
