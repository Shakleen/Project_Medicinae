package Logic;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class C_ViewInformation {
    @FXML private TableView<ObservableList<String>> FX_TableView;
    @FXML private ListView<String> FX_ListView_PhoneNumbers;
    @FXML private ProgressBar FX_ProgressBar;
    @FXML private Label FX_Label_Address;
    @FXML private ImageView FX_ImageView_InsertInfo;
    @FXML private ImageView FX_ImageView_EditInfo;
    @FXML private ImageView FX_ImageView_DeleteInfo;
    @FXML private ImageView FX_ImageView_AddColumn;
    @FXML private ImageView FX_ImageView_DeleteColumn;
    @FXML private BorderPane FX_BorderPane;


    public void initialize(){
        FX_ImageView_InsertInfo.setImage(new Image("/Images/Insert_Info.png"));
        FX_ImageView_InsertInfo.setFitHeight(50);
        FX_ImageView_InsertInfo.setFitWidth(50);
        FX_ImageView_InsertInfo.setPreserveRatio(true);

        FX_ImageView_EditInfo.setImage(new Image("/Images/Edit_Info.png"));
        FX_ImageView_EditInfo.setFitHeight(50);
        FX_ImageView_EditInfo.setFitWidth(50);
        FX_ImageView_EditInfo.setPreserveRatio(true);

        FX_ImageView_DeleteInfo.setImage(new Image("/Images/Delete_Info.png"));
        FX_ImageView_DeleteInfo.setFitHeight(50);
        FX_ImageView_DeleteInfo.setFitWidth(50);
        FX_ImageView_DeleteInfo.setPreserveRatio(true);

        FX_ImageView_AddColumn.setImage(new Image("/Images/Add_Column.png"));
        FX_ImageView_AddColumn.setFitHeight(50);
        FX_ImageView_AddColumn.setFitWidth(50);
        FX_ImageView_AddColumn.setPreserveRatio(true);

        FX_ImageView_DeleteColumn.setImage(new Image("/Images/Delete_Column.png"));
        FX_ImageView_DeleteColumn.setFitHeight(50);
        FX_ImageView_DeleteColumn.setFitWidth(50);
        FX_ImageView_DeleteColumn.setPreserveRatio(true);

        SetUpTableColumns();
        SetUpRecords(null);
    }


    /**
     * Method for setting up the table columns
     */
    private void SetUpTableColumns(){
        TableColumn<ObservableList<String>, String> IDColumn = new TableColumn("ID");
        IDColumn.setPrefWidth(20);
        IDColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(0)));

        TableColumn<ObservableList<String>, String> NameColumn = new TableColumn("Name");
        NameColumn.setPrefWidth(200);
        NameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(1)));

        TableColumn<ObservableList<String>, String> AgeColumn = new TableColumn("Age");
        AgeColumn.setPrefWidth(30);
        AgeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(2)));

        TableColumn<ObservableList<String>, String> SexColumn = new TableColumn("Sex");
        SexColumn.setPrefWidth(100);
        SexColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(3)));

        TableColumn<ObservableList<String>, String> ADateColumn = new TableColumn("Admission Date");
        ADateColumn.setPrefWidth(150);
        ADateColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(4)));

        FX_TableView.getColumns().addAll(IDColumn, NameColumn, AgeColumn, SexColumn, ADateColumn);

        for(int i = 0; i < B_Database.ListOfColumns.size(); ++i){
            AddColumn(i);
        }

        FX_TableView.setTableMenuButtonVisible(true);
        FX_TableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        FX_TableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ObservableList<String>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<String>> observableValue, ObservableList<String> strings, ObservableList<String> t1) {
                GetContactInformation();
            }
        });
    }


    /**
     * Method to add column to the table
     * @param i
     */
    private void AddColumn(int i){
        E_ColumnInfo columnInfo = B_Database.ListOfColumns.get(i);
        final int finalIdx = i + 5;
        TableColumn<ObservableList<String>, String> tableColumn = new TableColumn(columnInfo.ColumnName);
        tableColumn.setPrefWidth(columnInfo.ColumnName.length() * 10);
        tableColumn.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx))
        );
        FX_TableView.getColumns().add(tableColumn);
    }


    /**
     * Method for setting up record information to view in table
     */
    private void SetUpRecords(Integer ID){
        Task<ObservableList<ObservableList<String>>> Task_SetUpRecords = new Task<>() {
            ObservableList<ObservableList<String>> RecordData = FXCollections.observableArrayList();

            @Override
            protected ObservableList<ObservableList<String>> call() throws Exception {
                try{
                    int idx = 1, max = B_Database.B_database_instance.GetTotalRecordNumber();
                    System.out.println(max);
                    ResultSet rs = B_Database.B_database_instance.GetInformation(ID);

                    if (rs != null){
                        while(rs.next()){
                            ObservableList<String> Data = FXCollections.observableArrayList();
                            Data.add(rs.getString("PATIENT_ID"));
                            Data.add(rs.getString("PATIENT_NAME"));
                            Data.add(rs.getString("AGE"));
                            Data.add(rs.getString("SEX"));
                            Data.add(rs.getString("ADMISSION_DATE"));

                            for(int i = 0; i < B_Database.ListOfColumns.size(); ++i)
                                Data.add(rs.getString(B_Database.ListOfColumns.get(i).ColumnName));

                            RecordData.add(Data);
                            updateProgress(idx++, max);
                        }

                        return RecordData;
                    }
                } catch(SQLException e){
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void succeeded() {
                if (RecordData.size() > 0){
                    if (ID == null) FX_TableView.setItems(RecordData);
                    else            FX_TableView.getItems().add(RecordData.get(0));
                    FX_TableView.getSelectionModel().selectFirst();
                }
                FX_ProgressBar.setVisible(false);
            }
        };
        FX_ProgressBar.setVisible(true);
        FX_ProgressBar.progressProperty().bind(Task_SetUpRecords.progressProperty());
        new Thread(Task_SetUpRecords).start();
    }


    /**
     * Method for fetching contact information of the selected patient.
     */
    private void GetContactInformation(){
        Task<ObservableList<String>> Task_GetPhoneNumbers = new Task<ObservableList<String>>() {
            ObservableList<String> PhoneNumbers = FXCollections.observableArrayList();
            String Address;

            @Override
            protected ObservableList<String> call() throws Exception {
                String ID = FX_TableView.getSelectionModel().getSelectedItem().get(0);
                try{
                    ResultSet rs = B_Database.B_database_instance.GetPhoneNumbers(Integer.parseInt(ID));

                    if (rs != null){
                        while(rs.next()){
                            PhoneNumbers.add(rs.getString("PHONE_NUMBER"));
                        }
                        rs.close();

                        rs = B_Database.B_database_instance.GetAddress(Integer.parseInt(ID));
                        if (rs != null){
                            rs.next();
                            Address = rs.getString("ADDRESS");
                        }

                        return PhoneNumbers;
                    }
                } catch (SQLException e){
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void succeeded() {
                FX_Label_Address.setText(Address);

                if (PhoneNumbers.size() > 0){
                    FX_ListView_PhoneNumbers.setItems(PhoneNumbers);
                } else {
                    FX_ListView_PhoneNumbers.getItems().clear();
                }
            }
        };
        new Thread(Task_GetPhoneNumbers).start();
    }


    /**
     * Method that handles the information insert request.
     */
    @FXML private void HandleInsertRequest(){
        boolean con = B_DrawWindows.B_DrawWindows_instance.DrawDialog(
                "FXML_RecordHandling.fxml",
                "Insert new record",
                "Fill up the form with appropriate information",
                FX_BorderPane
        );

        if (con) {
            System.out.println("New stage drawn");
            C_RecordHandling c_recordHandling = B_DrawWindows.getFxmlLoader().getController();
            Optional<ButtonType> result = B_DrawWindows.getDialog().showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Task<Boolean> Task_HandleInsert = new Task<Boolean>() {
                    Boolean status = false;

                    @Override
                    protected Boolean call() throws Exception {
                        status = c_recordHandling.InsertInformation();
                        return status;
                    }

                    @Override
                    protected void succeeded() {
                        if (status) {
                            B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                    "Success",
                                    "Insertion successful",
                                    "Information successfully added to database.",
                                    "INFORMATION"
                            );
                            SetUpRecords(null);
                        } else {
                            B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                    "Failed",
                                    "Insertion failed",
                                    "Information could not be added to database.",
                                    "ERROR"
                            );
                        }
                    }

                    @Override
                    protected void failed() {
                        B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                "Failed",
                                "Insertion failed",
                                "Information could not be added to database.",
                                "ERROR"
                        );
                    }

                    @Override
                    protected void cancelled() {
                        B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                "Cancelled",
                                "Insertion cancelled",
                                "Task was cancelled by user.",
                                "INFORMATION"
                        );
                    }
                };
                new Thread(Task_HandleInsert).start();
            }
        }
    }


    @FXML private void HandleEditRequest(){
        boolean con = B_DrawWindows.B_DrawWindows_instance.DrawDialog(
                "FXML_RecordHandling.fxml",
                "Edit record data",
                "Fill up the form with new information",
                FX_BorderPane
        );

        if (con) {
            System.out.println("New stage drawn");
            C_RecordHandling c_recordHandling = B_DrawWindows.getFxmlLoader().getController();
            c_recordHandling.setFX_TextField_Name(FX_TableView.getSelectionModel().getSelectedItem().get(1));
            c_recordHandling.setFX_ComboBox_Age(FX_TableView.getSelectionModel().getSelectedItem().get(2));
            c_recordHandling.setFX_ComboBox_Sex(FX_TableView.getSelectionModel().getSelectedItem().get(3));
            c_recordHandling.setFX_TextField_Address(FX_Label_Address.getText());
            c_recordHandling.setFX_DatePicker_Admission(FX_TableView.getSelectionModel().getSelectedItem().get(4));
            String ID = FX_TableView.getSelectionModel().getSelectedItem().get(0);

            Optional<ButtonType> result = B_DrawWindows.getDialog().showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Task<Boolean> Task_UpdateInformation = new Task<Boolean>() {
                    Boolean Status;

                    @Override
                    protected Boolean call() throws Exception {
                        Status = c_recordHandling.EditInformation(Integer.parseInt(ID));
                        System.out.println(Status);
                        return Status;
                    }

                    @Override
                    protected void succeeded() {
                        if (Status) {
                            B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                    "Success",
                                    "Edited successfully",
                                    "Information successfully editted.",
                                    "INFORMATION"
                            );
                            SetUpRecords(null);
                        } else {
                            B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                    "Failed",
                                    "Edit failed",
                                    "Information could not be edited.",
                                    "ERROR"
                            );
                        }
                    }

                    @Override
                    protected void failed() {
                        B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                "Failed",
                                "Edit failed",
                                "Information could not be edited.",
                                "ERROR"
                        );
                    }

                    @Override
                    protected void cancelled() {
                        B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                "Cancelled",
                                "Edit cancelled",
                                "Task was cancelled by user.",
                                "INFORMATION"
                        );
                    }
                };
                new Thread(Task_UpdateInformation).start();
            }
        }
    }


    @FXML private void HandleDeleteRequest(){
        B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                "Confirmation",
                "Confirm delete operation",
                "Do you want to delete record?",
                "CONFIRMATION"
        );

        if (B_DrawWindows.getAlertResult().isPresent() && B_DrawWindows.getAlertResult().get() == ButtonType.OK){
            String ID = FX_TableView.getSelectionModel().getSelectedItem().get(0);
            String Name = FX_TableView.getSelectionModel().getSelectedItem().get(1);

            Task<Boolean> Task_DeleteRecord = new Task<Boolean>() {
                Boolean Status;

                @Override
                protected Boolean call() throws Exception {
                    Status = B_Database.B_database_instance.DeleteRecordData(Integer.parseInt(ID), Name);

                    return Status;
                }

                @Override
                protected void succeeded() {
                    if (Status) {
                        B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                "Status",
                                "Deleted successfully",
                                "Information successfully Deleted.",
                                "INFORMATION"
                        );
                        SetUpRecords(null);
                    } else {
                        B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                "Failed",
                                "Delete failed",
                                "Information could not be Deleted.",
                                "ERROR"
                        );
                    }
                }

                @Override
                protected void failed() {
                    B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                            "Failed",
                            "Delete failed",
                            "Information could not be Deleted.",
                            "ERROR"
                    );
                }

                @Override
                protected void cancelled() {
                    B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                            "Cancelled",
                            "Delete cancelled",
                            "Task was cancelled by user.",
                            "INFORMATION"
                    );
                }
            };

            new Thread(Task_DeleteRecord).start();
        }
    }


    @FXML private void HandleInsertColumnRequest(){
        boolean con = B_DrawWindows.B_DrawWindows_instance.DrawDialog(
                "FXML_InsertColumn.fxml",
                "Insert new column",
                "Please fill up the form to create new column",
                FX_BorderPane
        );

        if (con){
            C_InsertColumn c_Insert_column = B_DrawWindows.getFxmlLoader().getController();

            Optional<ButtonType> result = B_DrawWindows.getDialog().showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK){
                Task<E_ColumnInfo> Task_AddColumn = new Task<E_ColumnInfo>() {
                    E_ColumnInfo e_columnInfo;

                    @Override
                    protected E_ColumnInfo call() throws Exception {
                        e_columnInfo = c_Insert_column.ProcessColumnAdd();
                        return e_columnInfo;
                    }

                    @Override
                    protected void succeeded() {
                        if (e_columnInfo != null){
                            B_Database.ListOfColumns.add(e_columnInfo);
                            FX_TableView.getItems().clear();
                            FX_TableView.getColumns().clear();
                            SetUpTableColumns();
                            SetUpRecords(null);

                            B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                    "Addition successful!",
                                    "Added new column successfully",
                                    "New column was added.",
                                    "INFORMATION"
                            );
                        } else {
                            B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                    "Addition failed",
                                    "Addition failed",
                                    "Column could not be added.",
                                    "ERROR"
                            );
                        }
                    }

                    @Override
                    protected void failed() {
                        B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                "Addition failed",
                                "Addition failed",
                                "Column could not be added.",
                                "ERROR"
                        );
                    }
                };
                new Thread(Task_AddColumn).start();
            }
        }
    }


    @FXML private void HandleDeleteColumnRequest(){
        boolean con = B_DrawWindows.B_DrawWindows_instance.DrawDialog(
                "FXML_DeleteColumn.fxml",
                "Insert new column",
                "Please fill up the form to create new column",
                FX_BorderPane
        );

        if (con) {
            C_DeleteColumn c_Delete_column = B_DrawWindows.getFxmlLoader().getController();

            Optional<ButtonType> result = B_DrawWindows.getDialog().showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Task<Boolean> Task_DeleteColumns = new Task<Boolean>() {
                    Boolean Status = false;

                    @Override
                    protected Boolean call() throws Exception {
                        Status = c_Delete_column.DeleteColumns();
                        return Status;
                    }

                    @Override
                    protected void succeeded() {
                        if (Status){
                            B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                    "Deletion successful!",
                                    "Columns deleted successfully",
                                    "Columns were deleted with success.",
                                    "INFORMATION"
                            );
                            B_Database.B_database_instance.GetColumnInfoFromFile();
                            FX_TableView.getItems().clear();
                            FX_TableView.getColumns().clear();
                            SetUpTableColumns();
                            SetUpRecords(null);
                        } else {
                            B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                    "Deletion failed",
                                    "Deletion failed",
                                    "Column could not be Deletion.",
                                    "ERROR"
                            );
                        }
                    }

                    @Override
                    protected void failed() {
                        B_DrawWindows.B_DrawWindows_instance.DrawAlert(
                                "Deletion failed",
                                "Deletion failed",
                                "Column could not be Deletion.",
                                "ERROR"
                        );
                    }
                };
                new Thread(Task_DeleteColumns).start();
            }
        }
    }
}
