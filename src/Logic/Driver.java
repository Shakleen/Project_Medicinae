package Logic;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Driver extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("FXML_MainWindow.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1024, 615));
        primaryStage.show();
    }


    public static void main(String[] args) {
        Print();

        launch(args);
    }

    public static void Print(){
        try{
            ResultSet rs = B_Database.B_database_instance.GetBasicInformation();

            if (rs == null){
                System.out.println("Result set is null!");
            }
            else {
                System.out.println("ID\t\tName\t\tAge\t\tSex\t\tAddress\t\tAdmission_Date\n");
                while(rs.next()){
                    System.out.print(rs.getString("PATIENT_ID") + "\t\t");
                    System.out.print(rs.getString("PATIENT_NAME") + "\t\t");
                    System.out.print(rs.getString("AGE") + "\t\t");
                    System.out.print(rs.getString("SEX") + "\t\t");
                    System.out.print(rs.getString("ADDRESS") + "\t\t");
                    System.out.print(rs.getString("ADMISSION_DATE") + "\n");
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
