package Logic;

import java.util.ArrayList;

public class E_ColumnInfo {
    public String ColumnName;       // What the name of the column is
    public String ColumnValue;      // What the value of the column is
    public Integer ColumnSize;      // What the value of the column is
    public Integer ColumnType;      // What the type of column is. (VARCHAR - 1, Non VARCHAR - 2, Date - 3)
    public Boolean ConstraintHolder;// Whether the column is a constraint holder.
    public ArrayList<String> DomainValues;// The domain Values for the constraint.
    public Integer UpperLimit;
    public Integer LowerLimit;


    public E_ColumnInfo(String columnName, String columnValue, Integer columnType) {
        ColumnName = columnName;
        ColumnValue = columnValue;
        ColumnType = columnType;
    }

    public E_ColumnInfo(String columnName, Integer columnType, Boolean constraintHolder, Integer upperLimit, Integer lowerLimit) {
        ColumnName = columnName;
        ColumnType = columnType;
        ConstraintHolder = constraintHolder;
        UpperLimit = upperLimit;
        LowerLimit = lowerLimit;
    }

    public E_ColumnInfo(String columnName, Integer columnSize, Integer columnType, Boolean constraintHolder, ArrayList<String> domainValues) {
        ColumnName = columnName;
        ColumnSize = columnSize;
        ColumnType = columnType;
        ConstraintHolder = constraintHolder;
        DomainValues = domainValues;
    }
}
