package Logic;

import com.sun.media.jfxmediaimpl.platform.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class C_InsertInformation {
    ArrayList<ComboBox> NodeArray;

    @FXML private GridPane FX_GridPane;
    @FXML private TextField FX_TextField_Name;
    @FXML private TextField FX_TextField_Address;
    @FXML private ComboBox<String> FX_ComboBox_Age;
    @FXML private ComboBox<String> FX_ComboBox_Sex;
    @FXML private DatePicker FX_DatePicker_Admission;


    private final int X = 200;
    private final int Y = 25;
    private ObservableList<String> list;

    public void initialize(){
        FX_TextField_Name.setMinSize(X, Y);
        FX_TextField_Address.setMinSize(X, Y);

        FX_DatePicker_Admission.setValue(LocalDate.now());
        FX_DatePicker_Admission.setMinSize(X, Y);

        list = FXCollections.observableArrayList();
        for(Integer i = 0; i <= 200; ++i)   list.add(i.toString());
        FX_ComboBox_Age.setItems(list);
        FX_ComboBox_Age.setMinSize(X, Y);
        FX_ComboBox_Age.getSelectionModel().selectFirst();

        list = FXCollections.observableArrayList();
        list.add("MALE");   list.add("FEMALE");
        FX_ComboBox_Sex.setItems(list);
        FX_ComboBox_Sex.setMinSize(X, Y);
        FX_ComboBox_Sex.getSelectionModel().selectFirst();

        NodeArray = new ArrayList<>();
        int Pos_Label_Row = 6, Pos_ComboBox_Row = 6, Pos_Label_Column = 1, Pos_ComboBox_Column = 2;
        for(int i = 0; i < B_Database.ListOfColumns.size(); ++i){
            E_ColumnInfo columnInfo = B_Database.ListOfColumns.get(i);
            ComboBox<String> TempComboBox = new ComboBox<>();
            list = FXCollections.observableArrayList();

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
            TempComboBox.getSelectionModel().selectFirst();
            NodeArray.add(TempComboBox);
            FX_GridPane.add(new Label(columnInfo.ColumnName), Pos_Label_Column, Pos_Label_Row);
            FX_GridPane.add(NodeArray.get(i), Pos_ComboBox_Column, Pos_ComboBox_Row);

            if (i%2 == 0) {
                Pos_Label_Column = 3;
                Pos_ComboBox_Column = 4;
            } else {
                ++Pos_Label_Row;
                ++Pos_ComboBox_Row;
                Pos_Label_Column = 1;
                Pos_ComboBox_Column = 2;
            }
        }
    }


    /**
     * Method for sending database class information to add to dbms.
     * @return true if successful, false otherwise.
     */
    public boolean InsertInformation(){
        String Name = FX_TextField_Name.getText().trim();
        String Age = FX_ComboBox_Age.getSelectionModel().getSelectedItem();
        String Sex = FX_ComboBox_Sex.getSelectionModel().getSelectedItem();
        String Address = FX_TextField_Address.getText().trim();
        String Date = B_Database.B_database_instance.df.format(FX_DatePicker_Admission.getValue());

        ArrayList<E_ColumnInfo> Basic = new ArrayList<>(), Indepth = new ArrayList<>();
        Basic.add(new E_ColumnInfo("PATIENT_NAME", Name, 1));
        Basic.add(new E_ColumnInfo("AGE", Age, 2));
        Basic.add(new E_ColumnInfo("SEX", Sex, 1));
        Basic.add(new E_ColumnInfo("ADDRESS", Address, 1));
        Basic.add(new E_ColumnInfo("ADMISSION_DATE", Date, 3));

        System.out.println("Name : " + Name);
        System.out.println("Age : " + Age);
        System.out.println("Sex : " + Sex);
        System.out.println("Address : " + Address);
        System.out.println("Date of Admission : " + Date);

        E_ColumnInfo column;
        String Value;
        for(int i = 0; i < NodeArray.size(); ++i){
            column = B_Database.ListOfColumns.get(i);
            Value = NodeArray.get(i).getSelectionModel().getSelectedItem().toString();
            System.out.println(column.ColumnName + " : " + Value);
            Indepth.add(new E_ColumnInfo(column.ColumnName, Value, column.ColumnType));
        }

        return B_Database.B_database_instance.InsertInformation(Basic, Indepth, true);
    }

    public void setFX_TextField_Name(String Name) {
        this.FX_TextField_Name.setText(Name);
    }

    public void setFX_TextField_Address(String Address) {
        this.FX_TextField_Address.setText(Address);
    }

    public void setFX_ComboBox_Age(String Age) {
        int i = this.FX_ComboBox_Age.getItems().indexOf(Age);
        FX_ComboBox_Age.getSelectionModel().select(i);
    }

    public void setFX_ComboBox_Sex(String Sex) {
        int i = this.FX_ComboBox_Sex.getItems().indexOf(Sex);
        FX_ComboBox_Sex.getSelectionModel().select(i);
    }

    public void setFX_DatePicker_Admission(String Admission) {
        this.FX_DatePicker_Admission.setValue(LocalDate.parse(Admission, DateTimeFormatter.ofPattern("yyyy/MM/dd")));
    }

    public boolean EditInformation(Integer ID){
        ArrayList<E_ColumnInfo> EditColumnInfo = new ArrayList<>();
        String Name = FX_TextField_Name.getText().trim();
        String Age = FX_ComboBox_Age.getSelectionModel().getSelectedItem();
        String Sex = FX_ComboBox_Sex.getSelectionModel().getSelectedItem();
        String Address = FX_TextField_Address.getText().trim();
        String Date = B_Database.B_database_instance.df.format(FX_DatePicker_Admission.getValue());

        EditColumnInfo.add(new E_ColumnInfo("PATIENT_NAME", Name, 1));
        EditColumnInfo.add(new E_ColumnInfo("AGE", Age, 2));
        EditColumnInfo.add(new E_ColumnInfo("SEX", Sex, 1));
        EditColumnInfo.add(new E_ColumnInfo("ADDRESS", Address, 1));
        EditColumnInfo.add(new E_ColumnInfo("ADMISSION_DATE", Date, 3));

        E_ColumnInfo column;
        String Value;
        for(int i = 0; i < NodeArray.size(); ++i){
            column = B_Database.ListOfColumns.get(i);
            Value = NodeArray.get(i).getSelectionModel().getSelectedItem().toString();
            System.out.println(column.ColumnName + " : " + Value);
            EditColumnInfo.add(new E_ColumnInfo(column.ColumnName, Value, column.ColumnType));
        }

        try {
            return B_Database.B_database_instance.UpdateInformation(ID, EditColumnInfo);
        } catch (Defined_Exceptions e){
            e.printStackTrace();
        }

        return false;
    }
}
