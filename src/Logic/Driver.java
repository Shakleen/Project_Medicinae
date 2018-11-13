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
//        try{
//            B_Database.B_database_instance.UpdateDatabase();
//        } catch (Defined_Exceptions de){
//            System.out.println("Failed because of " + de.ExceptionName);
//        }
//        System.out.println(B_Database.B_database_instance.GetTotalColumnNumber());
//        Print();
        launch(args);
    }

    public static void Print(){
        try{
            ResultSet rs = B_Database.B_database_instance.GetInformation(null);

            if (rs == null){
                System.out.println("Result set is null!");
            }
            else {
                System.out.println("ID\t\tName\t\tAge\t\tSex\t\tAddress\t\tAdmission_Date\n");
                while(rs.next()){
                    System.out.print(rs.getString("PATIENT_ID") + "\n");
                    System.out.print(rs.getString("PATIENT_NAME") + "\n");
                    System.out.print(rs.getString("AGE") + "\n");
                    System.out.print(rs.getString("SEX") + "\n");
                    System.out.print(rs.getString("ADDRESS") + "\n");
                    System.out.print(rs.getString("ADMISSION_DATE") + "\n");
                    for(int i = 0; i < B_Database.ListOfColumns.size(); ++i){
                        System.out.println(rs.getString(B_Database.ListOfColumns.get(i).ColumnName));
                    }
                    System.out.println("\n\n");
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
