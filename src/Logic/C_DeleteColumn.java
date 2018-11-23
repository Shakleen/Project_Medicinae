package Logic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class C_DeleteColumn {
    @FXML private ListView<E_ColumnInfo> FX_ListView_AvailableColumns;
    @FXML private ListView<E_ColumnInfo> FX_ListView_DeleteColumns;
    @FXML private Button FX_Button_MoveToDeleteSingle;
    @FXML private Button FX_Button_MoveToDeleteAll;
    @FXML private Button FX_Button_MoveToAvailableSingle;
    @FXML private Button FX_Button_MoveToAvailableAll;

    public void initialize(){
        Callback<ListView<E_ColumnInfo>, ListCell<E_ColumnInfo>> callback = new Callback<>() {
            @Override
            public ListCell<E_ColumnInfo> call(ListView<E_ColumnInfo> e_columnInfoListView) {
                ListCell<E_ColumnInfo> cell = new ListCell<>(){
                    @Override
                    protected void updateItem(E_ColumnInfo e_columnInfo, boolean empty) {
                        super.updateItem(e_columnInfo, empty);

                        if (empty){
                            setText("");
                        } else {
                            setText(e_columnInfo.ColumnName);
                        }
                    }
                };

                return cell;
            }
        };
        FX_ListView_AvailableColumns.setItems(FXCollections.observableList(B_Database.ListOfColumns));
        FX_ListView_AvailableColumns.cellFactoryProperty().setValue(callback);
        FX_ListView_DeleteColumns.cellFactoryProperty().setValue(callback);

        FX_Button_MoveToDeleteSingle.setText(">");
        FX_Button_MoveToDeleteSingle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                E_ColumnInfo e_columnInfo = FX_ListView_AvailableColumns.getSelectionModel().getSelectedItem();
                FX_ListView_AvailableColumns.getItems().remove(e_columnInfo);
                FX_ListView_DeleteColumns.getItems().add(e_columnInfo);
            }
        });

        FX_Button_MoveToDeleteAll.setText(">>");
        FX_Button_MoveToDeleteAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FX_ListView_DeleteColumns.getItems().addAll(FX_ListView_AvailableColumns.getItems());
                FX_ListView_AvailableColumns.getItems().clear();
            }
        });

        FX_Button_MoveToAvailableSingle.setText("<");
        FX_Button_MoveToAvailableSingle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                E_ColumnInfo e_columnInfo = FX_ListView_DeleteColumns.getSelectionModel().getSelectedItem();
                FX_ListView_DeleteColumns.getItems().remove(e_columnInfo);
                FX_ListView_AvailableColumns.getItems().add(e_columnInfo);
            }
        });

        FX_Button_MoveToAvailableAll.setText("<<");
        FX_Button_MoveToAvailableAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FX_ListView_AvailableColumns.getItems().addAll(FX_ListView_DeleteColumns.getItems());
                FX_ListView_DeleteColumns.getItems().clear();
            }
        });
    }


    /**
     * Method for deleting columns.
     * @return true if successful. False otherwise.
     */
    public boolean DeleteColumns(){
        if (FX_ListView_DeleteColumns.getItems().isEmpty()){
            B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                    "Empty columns",
                    "No columns selected for deletion",
                    "There were no columns selected for deletion",
                    "ERROR"
            );
            return false;
        }

        boolean status = false;

        for(int i = 0; i < FX_ListView_DeleteColumns.getItems().size(); ++i){
            status = B_Database.DropColumn(FX_ListView_DeleteColumns.getItems().get(i).ColumnName);
        }

        return status;
    }
}
