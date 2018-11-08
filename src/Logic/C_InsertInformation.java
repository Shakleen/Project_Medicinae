package Logic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

public class C_InsertInformation {
    ArrayList<Node> ComboBoxArray;

    @FXML private GridPane FX_GridPane;
    @FXML private TextField FX_TextField_Name;
    @FXML private TextField FX_TextField_Address;
    @FXML private ComboBox<Integer> FX_ComboBox_Age;
    @FXML private ComboBox<String> FX_ComboBox_Sex;
    @FXML private DatePicker FX_DatePicker_Admission;

    public void initialize(){
        ComboBoxArray = new ArrayList<>();
        for(int i = 0; i < 10; ++i){
            ComboBoxArray.add(new ComboBox<>(FXCollections.observableArrayList("Temp", "Temp")));
            FX_GridPane.add(new Label("Temp combos"), 1, 6+i);
            FX_GridPane.add(ComboBoxArray.get(i), 2, 6+i);
        }
    }
}
