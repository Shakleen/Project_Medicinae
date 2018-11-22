package Logic;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class C_SearchHandling {
    @FXML private TableView<ObservableList<String>> FX_TableView;
    @FXML private TextField FX_TextField_Name;
    @FXML private TextField FX_TextField_Address;
    @FXML private ComboBox<String> FX_ComboBox_Age;
    @FXML private ComboBox<String> FX_ComboBox_Sex;
    @FXML private ScrollPane FX_ScrollPane;


    public void initialize(){
        ObservableList<String> Age = FXCollections.observableArrayList();
        Age.add("ALL");
        for(Integer i = 0; i <= 200; ++i) Age.add(i.toString());
        FX_ComboBox_Age.setItems(Age);
        FX_ComboBox_Age.getSelectionModel().selectFirst();
        FX_ComboBox_Sex.setItems(FXCollections.observableArrayList("ALL", "MALE", "FEMALE"));
        FX_ComboBox_Sex.getSelectionModel().selectFirst();

        SetUpTableColumns();
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


    @FXML private void HandleSearch(){
        Task<Boolean> Task_SearchInformation = new Task<Boolean>() {
            ObservableList<ObservableList<String>> RecordData = FXCollections.observableArrayList();
            Boolean Status = false;

            @Override
            protected Boolean call() throws Exception {
                String Name = FX_TextField_Name.getText();;
                String Age = FX_ComboBox_Age.getSelectionModel().getSelectedItem();
                String Sex = FX_ComboBox_Sex.getSelectionModel().getSelectedItem();
                String Address = FX_TextField_Address.getText();

                if (Name.equals("Enter Name") || Name.length() == 0)            Name = null;
                if (Age.equals("ALL"))                                          Age = null;
                if (Sex.equals("ALL"))                                          Sex = null;
                if (Address.equals("Enter Address") || Address.length() == 0)   Address = null;

                try{
                    ResultSet rs = B_Database.SearchInformation(Name, Age, Sex, Address);
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
                        }

                        Status = true;
                    }
                }
                catch (SQLException e){

                }
                return Status;
            }

            @Override
            protected void succeeded() {
                if (Status && RecordData.size() > 0){
                    FX_TableView.setItems(RecordData);
                    FX_TableView.getSelectionModel().selectFirst();
                }
            }
        };
        new Thread(Task_SearchInformation).start();
    }
}
