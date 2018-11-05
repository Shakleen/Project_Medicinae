package Logic;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * <h> Class for handling data base portion of the application </h>
 * <p>The purpose of this class is to handle database portion of the application. It stores
 * and retrieves information from the database.</p>
 * @author Shakleen Ishfar
 * @version 1.0.0.0
 * @since 05/11/2018
 */
public class B_Database {
    private Connection connection;  // Connection type object to get connection using driver.
    private String dbURL;           // URL for database used.
    private Statement statement;    // Statement type object to execute DDL and DML queries.
    private String query;           // Holds query for execution by statement object.
    private String values;
    private ResultSet resultSet;    // ResultSet type object to return database tables.
    private DateTimeFormatter df;

    // This is a static instance of the class. It will be the only object that will handle the communication
    // with the database.
    public static B_Database B_database_instance = new B_Database("ishrak", "dragonsword05");

    /**
     * Constructor for XEbase class objects. Creates connection with database.
     * By default 'classname' will be selected as DEMOCLASS and 'sectionname' as DEMOSECTION.
     * @param name String Username for the database account
     * @param password String Password that is allocated to the user
     */
    private B_Database (String name, String password){
        connection = null;
        df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try{
            Class.forName("oracle.jdbc.OracleDriver");
            dbURL = "jdbc:oracle:thin:@localhost:1521:xe";
            connection = DriverManager.getConnection(dbURL, name, password);

            if(connection != null)
                statement = connection.createStatement();
        }
        catch (ClassNotFoundException | SQLException ex){
            ex.printStackTrace();
        }
    }


    /**
     * Method for inserting information into the database.
     * @param Columns the column information for the record to be inserted.
     * @return true if successful. False otherwise.
     */
    public boolean InsertBasicInformation(ArrayList<E_ColumnInfo> Columns){
        query = "INSERT INTO BASIC_INFO (PATIENT_ID, ";
        values = ") VALUES (SEQ_PATIENT_ID.NEXTVAL, ";
        InsertQueryBuilder(Columns);
        System.out.println(query);
        return HandleUpdateExecution();
    }


    /**
     * Method for inserting patient phone numbers.
     * @param ID the D of the patient whose phone numbers are to be added.
     * @param PhoneNumbers the list of phone numbers to be added.
     * @return true if addition is successful. False otherwise.
     */
    public boolean InsertPhoneNumbers(Integer ID, ArrayList<String> PhoneNumbers){
        boolean con = false;

        for(int i = 0; i < PhoneNumbers.size(); ++i) {
            query = "INSERT INTO PATIENT_PHONE_NUMBERS (PATIENT_ID, PHONE_NUMBER) VALUES" +
                    "(" +
                        ID.toString() + ", " +
                        "'" + PhoneNumbers.get(i) + "'" +
                    ")";
            con = HandleUpdateExecution();
        }

        return con;
    }


    /**
     * Method for handling information updating relating to a single ID.
     * @param ID the patient ID whose information is to be updated.
     * @param Columns the column information to be updated.
     */
    public boolean UpdateBasicInformation(Integer ID, ArrayList<E_ColumnInfo> Columns){
        query = "UPDATE BASIC_INFO SET ";
        E_ColumnInfo column;

        // Go through each column name and information and add it to the query statement.
        for(int i = 0; i < Columns.size(); ++i){
            column = Columns.get(i);

            // ID can't be changed!
            if (column.ColumnName.equals("PATIENT_ID")) continue;

            // Column Name = Column Value
            query += column.ColumnName + " = " + AppendBasedOnType(column.ColumnValue, column.ColumnType);

            // Based on the index one of the following will be appended at each index.
            if (i < Columns.size()-1)       query += ", ";
            else                            query += " WHERE PATIENT_ID = " + ID.toString();
        }

        return HandleUpdateExecution();
    }


    /**
     * Method for getting information stored in the data base.
     * @return result stored in resultset if successful, null otherwise.
     */
    public ResultSet GetBasicInformation(){
        query = "SELECT * FROM BASIC_INFO";
        if (ResultSetHandler()) return resultSet;
        else                    return null;
    }


    /**
     * Method for getting information stored in the data base.
     * @param ID the ID of the patient whose basic information is to be retrieved.
     * @return result stored in resultset if successful, null otherwise.
     */
    public ResultSet GetBasicInformation(Integer ID){
        query = "SELECT * FROM BASIC_INFO WHERE PATIENT_ID = " + ID.toString();
        if (ResultSetHandler()) return resultSet;
        else                    return null;
    }


    /**
     * Used to execute statements directly read from file system.
     * @param Exec the statement to execute.
     */
    public void ExecuteStatement(String Exec){
        try{
            statement.execute(Exec);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }


    /**
     * Method for getting phone numbers of a specific patient ID.
     * @param ID the ID of the patient whose phone numbers are desired.
     * @return a result set containing the information if successfully retrieved or null otherwise.
     */
    public ResultSet GetPhoneNumbers(Integer ID){
        query = "SELECT PHONE_NUMBER FROM PATIENT_PHONE_NUMBERS WHERE PATIENT_ID = " + ID.toString();
        if (ResultSetHandler()) return resultSet;
        else                    return null;
    }


    /**
     * Handles execution of statement and produces a result set. Primarily used to retrieve data.
     * @return true if successful execution. false otherwise.
     */
    private boolean ResultSetHandler(){
        try{
            resultSet = statement.executeQuery(query);
            return true;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Handles execution of statement and produces a integer. Primarily used to update data.
     * @return true if integer produced is non-zero. false otherwise.
     */
    private boolean HandleUpdateExecution(){
        int Updates = 0;

        try{
            Updates = statement.executeUpdate(query);
        } catch (SQLException e){
            e.printStackTrace();
        }

        if (Updates == 0)
            return false;
        else{
            B_FileSystem.B_FileSystem_instance.WriteToFile(query + "\n", "SQL_Statements.sql", true);
            return true;
        }
    }


    /**
     * Handles direct statement execution. Used to execute statement read from the file.
     * @param Query the statement to execute.
     * @return true if successful. False otherwise.
     */
    public boolean HandleUpdateExecution(String Query){
        int Updates = 0;

        try{
            Updates = statement.executeUpdate(Query);
        } catch (SQLException e){
            e.printStackTrace();
        }

        if (Updates == 0)   return false;
        else                return true;
    }


    /**
     * Builds the insert query that will be executed.
     * @param Columns the column information to be inserted.
     */
    private void InsertQueryBuilder(ArrayList<E_ColumnInfo> Columns){
        E_ColumnInfo column;

        for(int i = 0; i < Columns.size(); ++i){
            column = Columns.get(i);        // get the i-th column info.
            query += column.ColumnName;     // Add the name to the query.
            values += AppendBasedOnType(column.ColumnValue, column.ColumnType);

            if (i != Columns.size()-1){
                query += ", ";
                values += ", ";
            }
            else{
                values += ")";
                query += values;
            }
        }
    }


    /**
     * Method for appending value based on type with correct formatting.
     * @param Value the value to be appended.
     * @param type the type of the column.
     * @return the string to be appended.
     */
    private String AppendBasedOnType(String Value, int type){
        switch(type){
            case 1: // Column type is VARCHAR2. Means we need quotations.
                return "'" + Value + "'";
            case 2: // Column type is not VARCHAR2. Means we do not need quotations.
                return Value;
            case 3: // Column type is date. Means we need proper formatting.
                return "TO_DATE('" + Value + "', 'dd/mm/yyyy')";
        }

        return "";
    }
}
