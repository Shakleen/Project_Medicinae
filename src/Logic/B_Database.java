package Logic;

import java.sql.*;
import java.time.LocalDate;
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
    private ResultSet resultSet;    // ResultSet type object to return database tables.
    String name;                    // Username of account.
    private String password;        // Password for account.
    private boolean error;          // Error state. Can be called at any time to check for errors.
    boolean connected;              // Connection state. Can be called at any time to check for connectivity.
    private int rt;                 // Private variable to return int type data by methods.
    private DateTimeFormatter df;

    // This is a static instance of the class. It will be the only object that will handle the communication
    // with the database.
    public static B_Database B_database_instance = new B_Database("ishrak", "dragonsword05");

    /**
     * Constructor for XEbase class objects. Creates connection with database.
     * By default 'classname' will be selected as DEMOCLASS and 'sectionname' as DEMOSECTION.
     * @param _username String Username for the database account
     * @param _password String Password that is allocated to the user
     */
    private B_Database (String _username, String _password){
        connection = null;
        error = connected = false;
        df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try{
            Class.forName("oracle.jdbc.OracleDriver");
            dbURL = "jdbc:oracle:thin:@localhost:1521:xe";
            name = _username;
            password = _password;
            connection = DriverManager.getConnection(dbURL, name, password);

            if(connection != null) {
                connected = true;
                statement = connection.createStatement();
            }
            else connected = false;
        }
        catch (ClassNotFoundException | SQLException ex){
            error = true;
            ex.printStackTrace();
        }
    }


    /**
     * Method for inserting information into the database.
     * @param Name The patient's name.
     * @param Age The patient's age.
     * @param Sex The patient's sex (MALE or FEMALE)
     * @param Address The patient's address.
     * @param AdmissionDate The patient's admission date.
     * @return
     */
    public boolean InsertBasicInformation(String Name, Integer Age, String Sex, String Address, LocalDate AdmissionDate){
        query = "INSERT INTO BASIC_INFO (PATIENT_ID, PATIENT_NAME, AGE, SEX, ADDRESS, ADMISSION_DATE)" + "VALUES" +
                "(" +
                            "SEQ_PATIENT_ID.NEXTVAL, " +
                            "'" + Name + "', " +
                            Age.toString() + ", " +
                            "'" + Sex.toUpperCase() + "', " +
                            "'" + Address + "', " +
                            "TO_DATE('" + df.format(AdmissionDate) + "', 'dd/mm/yyyy')" +
                ")";

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
     * @param Column_Names the name of the columns to be updated.
     * @param Column_Type the type (String = true, Non-string = false) of the columns to be updated.
     * @param Column_Information the information the column should have.
     * @return true if information update was successful. False otherwise.
     */
    public boolean UpdateBasicInformation(Integer ID, ArrayList<String> Column_Names, ArrayList<Boolean> Column_Type, ArrayList<String> Column_Information){
        query = "UPDATE BASIC_INFO SET ";

        // Go through each column name and information and add it to the query statement.
        for(int i = 0; i < Column_Names.size(); ++i){
            // Column is going to take string type values. So quotations are necessary.
            if (Column_Type.get(i))     query += Column_Names.get(i) + " = '" + Column_Information.get(i) + "'";
            // Column is not going to take string type values. So quotations are not necessary.
            else                        query += Column_Names.get(i) + " = " + Column_Information.get(i);

            // Based on the index one of the following will be appended at each index.
            if (i < Column_Names.size()-1)  query += ", ";
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
     * Handles execution of statement and produces a result set.
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
     * Handles execution of statement and produces a integer.
     * @return true if integer produced is non-zero. false otherwise.
     */
    private boolean HandleUpdateExecution(){
        int Updates = 0;

        try{
            Updates = statement.executeUpdate(query);
        } catch (SQLException e){
            e.printStackTrace();
        }

        if (Updates == 0)   return false;
        else                return true;
    }
}
