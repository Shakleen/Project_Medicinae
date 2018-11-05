package Logic;

public class E_ColumnInfo {
    public String ColumnName;       // What the name of the column is
    public String ColumnValue;      // What the value of the column is
    public Integer ColumnType;      // What the type of column is. (VARCHAR - 1, Non VARCHAR - 2, Date - 3)

    public E_ColumnInfo(String columnName, String columnValue, Integer columnType) {
        ColumnName = columnName;
        ColumnValue = columnValue;
        ColumnType = columnType;
    }
}
