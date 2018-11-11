package Logic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Set;

public class C_ViewInformation {
    @FXML private TableView<ObservableList<String>> FX_TableView;
    @FXML private ListView<String> FX_ListView_PhoneNumbers;
    @FXML private ScrollPane FX_ScrollPane;
    
    private ArrayList<TableColumn> TableColumnList;

    public void initialize(){
        SetUpTableColumns();
    }

    /**
     * Method for setting up the table columns
     */
    private void SetUpTableColumns(){
        TableColumn IDColumn = new TableColumn("ID");                   IDColumn.setPrefWidth(20);
        TableColumn NameColumn = new TableColumn("Name");               NameColumn.setPrefWidth(200);
        TableColumn AgeColumn = new TableColumn("Age");                 AgeColumn.setPrefWidth(30);
        TableColumn SexColumn = new TableColumn("Sex");                 SexColumn.setPrefWidth(100);
        TableColumn ADateColumn = new TableColumn("Admission Date");    ADateColumn.setPrefWidth(150);
        FX_TableView.getColumns().addAll(IDColumn, NameColumn, AgeColumn, SexColumn, ADateColumn);

        for(int i = 0; i < B_Database.ListOfColumns.size(); ++i){
            E_ColumnInfo columnInfo = B_Database.ListOfColumns.get(i);
            TableColumn tableColumn = new TableColumn(columnInfo.ColumnName);
            tableColumn.setPrefWidth(columnInfo.ColumnName.length() * 10);
            FX_TableView.getColumns().add(tableColumn);
        }
        FX_TableView.setTableMenuButtonVisible(true);
    }
}
