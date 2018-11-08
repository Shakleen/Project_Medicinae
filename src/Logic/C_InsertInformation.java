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

    private final int X = 200;
    private final int Y = 25;

    public void initialize(){
        FX_TextField_Name.setMinSize(X, Y);
        FX_TextField_Address.setMinSize(X, Y);
        FX_ComboBox_Age.setMinSize(X, Y);
        FX_ComboBox_Sex.setMinSize(X, Y);
        FX_DatePicker_Admission.setMinSize(X, Y);

        ComboBoxArray = new ArrayList<>();
        for(int i = 0; i < B_Database.ListOfColumns.size(); ++i){
            E_ColumnInfo columnInfo = B_Database.ListOfColumns.get(i);
            ComboBox<String> TempComboBox = new ComboBox<>();
            ObservableList<String> list = FXCollections.observableArrayList();

            switch (columnInfo.ColumnType){
                case 1:
                    for (int j = 0; j < columnInfo.DomainValues.size(); ++j) list.add(columnInfo.DomainValues.get(j));
                    break;
                case 2:
                    for (Integer j = columnInfo.LowerLimit; j <= columnInfo.UpperLimit; ++j) list.add(j.toString());
                    break;
            }

            TempComboBox.setMinSize(X, Y);
            TempComboBox.setItems(list);
            ComboBoxArray.add(TempComboBox);
            FX_GridPane.add(new Label(columnInfo.ColumnName), 1, 6+i);
            FX_GridPane.add(ComboBoxArray.get(i), 2, 6+i);
        }
    }
}
