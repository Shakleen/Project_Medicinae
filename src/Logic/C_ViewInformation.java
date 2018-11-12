package Logic;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class C_ViewInformation {
    @FXML private TableView<ObservableList<String>> FX_TableView;
    @FXML private ListView<String> FX_ListView_PhoneNumbers;
    @FXML private ProgressBar FX_ProgressBar;
    @FXML private Label FX_Label_Address;
    @FXML private ImageView FX_ImageView_InsertInfo;
    @FXML private Button FX_Button_Insert;
    @FXML private BorderPane FX_BorderPane;


    public void initialize(){
        FX_ImageView_InsertInfo.setImage(new Image("/Images/Insert_Info.png"));
        FX_ImageView_InsertInfo.setFitHeight(50);
        FX_ImageView_InsertInfo.setFitWidth(50);
        FX_ImageView_InsertInfo.setPreserveRatio(true);
        // Event handler to handle insert button click
        FX_Button_Insert.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                boolean con = B_DrawWindows.B_DrawWindows_instance.DrawNewStage(
                        "FXML_InsertInformation.fxml",
                        "Insert new record",
                        600,
                        700
                );

                if (con) System.out.println("New stage drawn");
                else System.out.println("Draw failed!");
            }
        });
        SetUpTableColumns();
        SetUpRecords();
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
            E_ColumnInfo columnInfo = B_Database.ListOfColumns.get(i);
            final int finalIdx = i + 5;
            TableColumn<ObservableList<String>, String> tableColumn = new TableColumn(columnInfo.ColumnName);
            tableColumn.setPrefWidth(columnInfo.ColumnName.length() * 10);
            tableColumn.setCellValueFactory(param ->
                    new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx))
            );
            FX_TableView.getColumns().add(tableColumn);
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
     * Method for setting up record information to view in table
     */
    private void SetUpRecords(){
        Task<ObservableList<ObservableList<String>>> Task_SetUpRecords = new Task<>() {
            ObservableList<ObservableList<String>> RecordData = FXCollections.observableArrayList();

            @Override
            protected ObservableList<ObservableList<String>> call() throws Exception {
                try{
                    int idx = 1, max = B_Database.B_database_instance.GetTotalRecordNumber();
                    System.out.println(max);
                    ResultSet rs = B_Database.B_database_instance.GetInformation();

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
                    FX_TableView.setItems(RecordData);
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
}
