package Logic;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;

public class C_InsertColumn {

    @FXML private ComboBox<String> FX_ComboBox_Type;
    @FXML private TextField FX_TextField_ColumnName;
    @FXML private TextField FX_TextField_UpperLimit;
    @FXML private TextField FX_TextField_LowerLimit;
    @FXML private ListView<TextField> FX_ListView_TextFields;
    @FXML private ListView<Button> FX_ListView_Buttons;
    @FXML private Button FX_Button_AddNew;
    @FXML private Slider FX_Slider_ColumnSize;
    @FXML private Label FX_Label_SliderValue;



    public void initialize(){
        FX_ComboBox_Type.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (FX_ComboBox_Type.getSelectionModel().getSelectedItem().equals("String")){
                    System.out.println("String type");
                    FX_TextField_UpperLimit.setDisable(true);
                    FX_TextField_LowerLimit.setDisable(true);
                    FX_ListView_TextFields.setDisable(false);
                    FX_ListView_Buttons.setDisable(false);
                    FX_Button_AddNew.setDisable(false);
                    FX_Slider_ColumnSize.setDisable(false);
                } else if (FX_ComboBox_Type.getSelectionModel().getSelectedItem().equals("Number")){
                    System.out.println("Number type");
                    FX_TextField_UpperLimit.setDisable(false);
                    FX_TextField_LowerLimit.setDisable(false);
                    FX_ListView_TextFields.setDisable(true);
                    FX_Slider_ColumnSize.setDisable(true);
                    FX_ListView_Buttons.setDisable(true);
                    FX_Button_AddNew.setDisable(true);
                }
            }
        });
        FX_ComboBox_Type.getSelectionModel().selectFirst();
        FX_Slider_ColumnSize.setMinWidth(250);
        FX_Slider_ColumnSize.setShowTickLabels(true);
        FX_Slider_ColumnSize.setValueChanging(true);
        FX_Slider_ColumnSize.setSnapToTicks(true);
        FX_Slider_ColumnSize.setBlockIncrement(25);
        FX_Slider_ColumnSize.setMajorTickUnit(25);
        FX_Slider_ColumnSize.setMinorTickCount(25);
        FX_Slider_ColumnSize.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                FX_Label_SliderValue.setText(Double.toString(FX_Slider_ColumnSize.getValue()));
            }
        });
    }


    @FXML private void HandleAddNewConstraint(){
        TextField textField = new TextField();
        Button button = new Button("X");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Button Caller = ((Button) actionEvent.getSource());
                int idx = FX_ListView_Buttons.getItems().indexOf(Caller);
                FX_ListView_Buttons.getItems().remove(idx);
                FX_ListView_TextFields.getItems().remove(idx);
            }
        });
        FX_ListView_TextFields.getItems().add(textField);
        FX_ListView_Buttons.getItems().add(button);
    }



    private  boolean PerformChecks(String ColumnName){
        if (ColumnName.length() == 0){
            B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                    "Empty Column Name",
                    "Column Name can not be empty",
                    "Please set a unique column name",
                    "ERROR"
            );

            return false;
        } else if (B_Database.ListOfColumns.contains(ColumnName)){
            B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                    "Column Already Exists!",
                    "Column Name must be unique",
                    "Please set a unique column name",
                    "ERROR"
            );

            return false;
        } else if (ColumnName.contains(" ")) {
            B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                    "White space!",
                    "Column Name can't contain white space",
                    "Use underscore instead of white space",
                    "ERROR"
            );

            return false;
        }

        return true;
    }


    public E_ColumnInfo ProcessColumnAdd(){
        String ColumnName = FX_TextField_ColumnName.getText();
        String ColumnType = FX_ComboBox_Type.getSelectionModel().getSelectedItem();

        if (PerformChecks(ColumnName)){
            try {
                if (ColumnType.equals("String")) {
                    int size = FX_ListView_TextFields.getItems().size();
                    int ColumnSize = ((int) FX_Slider_ColumnSize.getValue());

                    if (size == 0) {
                        B_Database.AddColumn(ColumnName, 1, ColumnSize, true);
                    }
                    else {
                        ArrayList<String> DomainValues = new ArrayList<>();
                        for(int i = 0; i < FX_ListView_TextFields.getItems().size(); ++i){
                            DomainValues.add(FX_ListView_TextFields.getItems().get(i).getText().toString());
                        }

                        B_Database.AddColumnWithCheck(ColumnName, 1, ColumnSize, DomainValues, true);
                    }

                    return new E_ColumnInfo(ColumnName, null, 1);
                } else if (ColumnType.equals("Number")) {
                    String UpperLimit = FX_TextField_UpperLimit.getText().trim().toString();
                    String LowerLimit = FX_TextField_LowerLimit.getText().trim().toString();

                    if (UpperLimit.length() == 0 || LowerLimit.length() == 0){
                        B_Database.AddColumn(ColumnName, 2, 0, true);
                    }
                    else {
                        B_Database.AddColumnWithCheck(ColumnName, 2, Integer.parseInt(UpperLimit), Integer.parseInt(LowerLimit), true);
                    }

                    return new E_ColumnInfo(ColumnName, null, 2);
                }
            } catch (Defined_Exceptions e){
                e.printStackTrace();
            }
        }

        return null;
    }
}
