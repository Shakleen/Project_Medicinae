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
    private Statement statement;    // Statement type object to execute DDL and DML queries.
    private String query;           // Holds query for execution by statement object.
    private String values;          // Used to store column values.
    private ResultSet resultSet;    // ResultSet type object to return database tables.
    private final String ColumnFileName = "ColumnNames.txt";        // Name of the file where column information is kept.
    private final String RecordFileName = "A.txt";        // Name of the file where column information is kept.
    private final String ContactFileName = "AC.txt";        // Name of the file where column information is kept.
    private final String WordSeparator = "#";
    private final String LineSeparator = "|";
    private final String Seperator = WordSeparator + LineSeparator + '\n' + WordSeparator;
    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private final String DateFormat = "YYYY/MM/DD";
    public static ArrayList<E_ColumnInfo> ListOfColumns;

    // This is a static instance of the class. It will be the only object that will handle the communication with the database.
    public static B_Database B_database_instance = new B_Database("ishrak", "dragonsword05");

    /**
     * Constructor for XEbase class objects. Creates connection with database.
     * By default 'classname' will be selected as DEMOCLASS and 'sectionname' as DEMOSECTION.
     * @param name String Username for the database account
     * @param password String Password that is allocated to the user
     */
    private B_Database (String name, String password){
        connection = null;
        try{
            Class.forName("oracle.jdbc.OracleDriver");
            String dbURL = "jdbc:oracle:thin:@localhost:1521:xe";
            connection = DriverManager.getConnection(dbURL, name, password);
            if(connection != null){
                statement = connection.createStatement();
                SetupDatabaseForUsage();
            }
        }
        catch (ClassNotFoundException | SQLException ex){
            ex.printStackTrace();
        }
    }


    /**
     * Method for checking and setting up the database.
     * @return true if successful. false otherwise.
     */
    private boolean SetupDatabaseForUsage(){
        try{
            boolean BASIC_INFO = false, IN_DEPTH_INFO = false;
            ResultSet rs = statement.executeQuery("SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE 'BASIC_INFO'");
            BASIC_INFO = rs.next();
            rs = statement.executeQuery("SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE 'IN_DEPTH_INFO'");
            IN_DEPTH_INFO = rs.next();

            ListOfColumns = new ArrayList<>();
            GetColumnInfoFromFile();

            if (BASIC_INFO == false && IN_DEPTH_INFO == false)  BASIC_INFO = IN_DEPTH_INFO = UpdateDatabase();
        } catch (Defined_Exceptions | SQLException de){

        }
        return false;
    }


    /**
     * Used to update the information stored in the database from the sql file.
     * Used only when the application had data stored before an unexpected failure.
     * @throws Defined_Exceptions an exception is thrown when no account is logged in.
     * @return true if successful, false otherwise.
     */
    public boolean UpdateDatabase() throws Defined_Exceptions{
        // Checks to see if there is a connection before proceeding.
        if (connection == null) throw new Defined_Exceptions("SQL_ACCOUNT_ERROR");

        // Run table and column related sql statements from file
        if (CreateTableStructure()) {
            // Add the columns from column name file.
            if (SetupTableColumnsFromFile()) {
                // Run record related sql statements from file
                if (UpdateRecords())
                    return GetPhoneNumbersFromFile();
                System.out.println("UpdateDatabase - Couldn't add records to table!");
            } else System.out.println("UpdateDatabase - Couldn't create columns!");
        } else System.out.println("UpdateDatabase - Couldn't execute table creation statements!");

        return false;
    }


    /**
     * Method to get the column information from file.
     * @return true if successful, false otherwise.
     */
    private boolean SetupTableColumnsFromFile() throws Defined_Exceptions{
        if (connection == null)
            return false;

        for(int i = 0; i < ListOfColumns.size(); ++i){
            E_ColumnInfo column = ListOfColumns.get(i);

            switch (column.ColumnType){
                case 1:
                    AddColumnWithCheck(column.ColumnName, column.ColumnType, column.ColumnSize, column.DomainValues, false);
                     break;
                case 2:
                    AddColumnWithCheck(column.ColumnName, column.ColumnType, column.UpperLimit, column.LowerLimit, false);
                    break;
                case 3:// TODO Date
                    break;
            }
        }

        return true;
    }


    /**
     * Method for retrieving column related information from file and storing them into a list.
     * @return true if successful, false otherwise.
     */
    public boolean GetColumnInfoFromFile(){
        String ColumnName = null, LineRead = null, temp = null;
        Integer ColumnType = null, ColumnSize = null;
        ArrayList<String> DomainValues = null;
        boolean EndOfLine = false;

        // Open column file for reading.
        if (B_FileSystem.B_FileSystem_instance.SetUpFileReader(ColumnFileName, "#")) {
            ListOfColumns.clear();

            while (true) {
                // Getting name
                ColumnName = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                if (ColumnName == null) break;
                B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                // Getting type
                ColumnType = B_FileSystem.B_FileSystem_instance.ReadFromFileNextInt();
                B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                // Getting parameters
                switch (ColumnType) {
                    // Column is of type VARCHAR2
                    case 1:
                        // Get column size for VARCHAR2
                        ColumnSize = B_FileSystem.B_FileSystem_instance.ReadFromFileNextInt();
                        System.out.println("Name = " + ColumnName + " type = " + ColumnType + " Size = " + ColumnSize);
                        B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                        // New array list for storing domain values gotten from file.
                        DomainValues = new ArrayList<>();

                        // Variable for keeping checking condition value of whether end of file was reached.
                        EndOfLine = false;

                        do {
                            // Read the line from file.
                            LineRead = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                            B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                            // Check if the line read has the character full stop indicating end of line.
                            EndOfLine = LineRead.contains("|");
                            if (EndOfLine) break;

                            // If the line read is not null and is greater than 0 size add to domain.
                            if (LineRead != null) if (LineRead.length() > 0) DomainValues.add(LineRead);
                        } while (LineRead != null);

                        ListOfColumns.add(new E_ColumnInfo(ColumnName, ColumnSize, ColumnType, true, DomainValues));
                        break;

                    // Column is of type number. So constraint parameters will be only Upper and lower limit.
                    case 2:
                        Integer LowerLimit = B_FileSystem.B_FileSystem_instance.ReadFromFileNextInt();
                        B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();
                        Integer UpperLimit = B_FileSystem.B_FileSystem_instance.ReadFromFileNextInt();
                        B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();
                        B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                        B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();
                        System.out.println("Name = " + ColumnName + " type = " + ColumnType + " U = " + UpperLimit + " L = " + LowerLimit);
                        ListOfColumns.add(new E_ColumnInfo(ColumnName, ColumnType, true, UpperLimit, LowerLimit));
                        break;

                    // TODO Column is of type Date
                    case 3:
                        break;
                }
            }

            return true;
        }

        return false;
    }


    /**
     * Method for creating table structure.
     * @return true when success otherwise false.
     */
    private boolean CreateTableStructure(){
        query = "CREATE TABLE BASIC_INFO(" +
                    "PATIENT_ID NUMBER," +
                    "PATIENT_NAME VARCHAR2(100)," +
                    "AGE NUMBER," +
                    "SEX VARCHAR2(25)," +
                    "ADDRESS VARCHAR2(200)," +
                    "ADMISSION_DATE DATE," +
                    "CONSTRAINT PK_PATIENT_ID PRIMARY KEY (PATIENT_ID)," +
                    "CONSTRAINT CHK_SEX CHECK (SEX IN ('MALE', 'FEMALE'))," +
                    "CONSTRAINT CHK_AGE CHECK (AGE >= 0)," +
                    "CONSTRAINT UN_PATIENT UNIQUE(PATIENT_NAME, AGE, SEX, ADDRESS, ADMISSION_DATE)" +
                ")";

        if (ExecuteStatement(query, 0)){
            query = "CREATE SEQUENCE SEQ_PATIENT_ID MINVALUE 1 START WITH 1 INCREMENT BY 1 CACHE 10";

            if (ExecuteStatement(query, 0)){
                query = "CREATE TABLE PATIENT_PHONE_NUMBERS(" +
                            "PATIENT_ID NUMBER," +
                            "PHONE_NUMBER VARCHAR2(20)," +
                            "CONSTRAINT FK_PATIENT_ID FOREIGN KEY (PATIENT_ID) REFERENCES BASIC_INFO" +
                        ")";

                if (ExecuteStatement(query, 0)){
                    query = "CREATE TABLE IN_DEPTH_INFO(" +
                                "PATIENT_ID NUMBER," +
                                "CONSTRAINT FK_IND_PATIENT_ID FOREIGN KEY (PATIENT_ID) REFERENCES BASIC_INFO (PATIENT_ID)" +
                            ")";

                    return ExecuteStatement(query, 0);
                }
            }
        }

        return false;
    }


    /**
     * Method for updating record from file.
     * @return true if successful, false otherwise.
     */
    private boolean UpdateRecords(){
        if (B_FileSystem.B_FileSystem_instance.SetUpFileReader(RecordFileName, WordSeparator)){
                String Name = null, Value = null;
                Integer Type = null;
                Boolean Break = false;

                while(true) {
                    ArrayList<E_ColumnInfo> BasicColumnValues = new ArrayList<>();
                    ArrayList<E_ColumnInfo> InDepthColumnValues = new ArrayList<>();

                    for (int i = 1; ; ++i) {
                        // Reading the column name
                        Name = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                        if (Name == null) {Break = true; break;}
                        else if (Name.contains(LineSeparator)) break;
                        B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                        // Reading the column value
                        Value = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                        B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                        // Reading the column type
                        Type = B_FileSystem.B_FileSystem_instance.ReadFromFileNextInt();
                        B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                        System.out.println(Name + " " + Value + " " + Type);

                        // Add to appropriate table column
                        if (i <= 5) BasicColumnValues.add(new E_ColumnInfo(Name, Value, Type));
                        else InDepthColumnValues.add(new E_ColumnInfo(Name, Value, Type));
                    }

                    // Insert record.
                    if (Break == false) InsertInformation(BasicColumnValues, InDepthColumnValues, false);
                    else                break;
                }

                return true;
            }

        return false;
    }


    /**
     * Method for inserting information related to a patient.
     * @param BasicColumns the basic information.
     * @param InDepthColumns the in depth information.
     * @return true if successful, false otherwise.
     */
    public boolean InsertInformation(ArrayList<E_ColumnInfo> BasicColumns, ArrayList<E_ColumnInfo> InDepthColumns, boolean WrtieCondition){
        try{
            if (InsertBasicInformation(BasicColumns, true)){
                if (InsertBasicInformation(InDepthColumns, false)){
                    if (WrtieCondition) {
                        String Temp = "";
                        E_ColumnInfo columnInfo = null;

                        // Adding the basic table column values
                        for (int i = 0; i < BasicColumns.size(); ++i) {
                            columnInfo = BasicColumns.get(i);
                            Temp += columnInfo.ColumnName + WordSeparator + columnInfo.ColumnValue + WordSeparator + columnInfo.ColumnType + WordSeparator;
                        }

                        // Adding the in depth column values
                        for (int i = 0; i < InDepthColumns.size(); ++i) {
                            columnInfo = InDepthColumns.get(i);
                            Temp += columnInfo.ColumnName + WordSeparator + columnInfo.ColumnValue + WordSeparator + columnInfo.ColumnType + WordSeparator;
                        }

                        Temp += LineSeparator + "\n" + WordSeparator;

                        B_FileSystem.B_FileSystem_instance.WriteToFile(Temp, RecordFileName, true);
                    }

                    return true;
                }
            }
        } catch (Defined_Exceptions e){
            System.out.println("InsertInformation - Problem in inserting information");
        }

        return false;
    }


    /**
     * Method for inserting information into the database.
     * @param Columns the column information for the record to be inserted.
     * @param Type Whether the information is for Basic table or in-depth table.
     * @throws Defined_Exceptions when there are no column information is given or when columns is null.
     * @return true if successful. False otherwise.
     */
    private boolean InsertBasicInformation(ArrayList<E_ColumnInfo> Columns, boolean Type) throws Defined_Exceptions{
        // Check to see if columns is null.
        if (Columns == null)            throw new Defined_Exceptions("NULL_COLUMN_ARRAY");
        // No column information given exception
        else if (Columns.size() == 0)   throw new Defined_Exceptions("ZERO_COLUMNS_FOR_RECORD");

        // SELECT LAST_NUMBER FROM ALL_SEQUENCES WHERE SEQUENCE_NAME = 'SEQ_PATIENT_ID' TODO
        if (Type) {
            query = "INSERT INTO BASIC_INFO (PATIENT_ID, ";
            values = ") VALUES (SEQ_PATIENT_ID.NEXTVAL, ";
        } else {
            query = "INSERT INTO IN_DEPTH_INFO (PATIENT_ID, ";
            values = ") VALUES ((SELECT MAX(PATIENT_ID) FROM BASIC_INFO), ";
        }
        InsertQueryBuilder(Columns);
        return HandleUpdateExecution();
    }

    /**
     * Method for inserting patient phone numbers.
     * @param ID the D of the patient whose phone numbers are to be added.
     * @param PhoneNumbers the list of phone numbers to be added.
     * @throws Defined_Exceptions when null array or zero phone numbers have been given.
     * @return true if addition is successful. False otherwise.
     */
    public boolean InsertPhoneNumbers(Integer ID, ArrayList<String> PhoneNumbers, boolean WriteCondition) throws Defined_Exceptions{
        // Check if null
        if (PhoneNumbers == null)               throw new Defined_Exceptions("NULL_COLUMN_ARRAY");
        // Check if zero elements
        else if (PhoneNumbers.size() == 0)      throw new Defined_Exceptions("ZERO_COLUMNS_FOR_RECORD");

        boolean con = false;
        String LineToWrite = "";
        for (int i = 0; i < PhoneNumbers.size(); ++i) {
            query = "INSERT INTO PATIENT_PHONE_NUMBERS (PATIENT_ID, PHONE_NUMBER) VALUES" +
                    "(" +
                    ID.toString() + ", " +
                    "'" + PhoneNumbers.get(i) + "'" +
                    ")";
            con = HandleUpdateExecution();

            LineToWrite += PhoneNumbers.get(i);

            if (i != PhoneNumbers.size() - 1)   LineToWrite += WordSeparator;
            else                                LineToWrite += WordSeparator + LineSeparator + '\n' + WordSeparator;
        }

        if (con && WriteCondition) {
            con = WritePhoneNumbersToFile(ID, LineToWrite);
        }
        return con;
    }


    /**
     * Method for inserting patient phone numbers.
     * @param PhoneNumbers the list of phone numbers to be added.
     * @throws Defined_Exceptions when null array or zero phone numbers have been given.
     * @return true if addition is successful. False otherwise.
     */
    public boolean InsertPhoneNumbers(ArrayList<E_ColumnInfo> Basic_Info, ArrayList<String> PhoneNumbers, boolean WriteCondition){
        String Name = Basic_Info.get(0).ColumnValue;
        String Age = Basic_Info.get(1).ColumnValue;
        String Sex = Basic_Info.get(2).ColumnValue;
        String Address = Basic_Info.get(3).ColumnValue;
        String AdmissionDate = Basic_Info.get(4).ColumnValue;
        Integer ID = 0;

        try{
            query = "SELECT PATIENT_ID FROM BASIC_INFO " +
                    "WHERE PATIENT_NAME = '" + Name + "' AND " +
                    "AGE = " + Age + " AND " +
                    "SEX = '" + Sex + "' AND " +
                    "ADDRESS = '" + Address + "' AND " +
                    "ADMISSION_DATE = TO_DATE('" + AdmissionDate + "', '" + DateFormat + "')";
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()){
                ID = rs.getInt("PATIENT_ID");

                if (ID >= 1){
                    return InsertPhoneNumbers(ID, PhoneNumbers, WriteCondition);
                }
            }
        } catch (SQLException | Defined_Exceptions e){
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Method for writing phone numbers to files.
     * Should be called only after database has been populated with records.
     */
    private boolean WritePhoneNumbersToFile(Integer ID, String PhoneNumbers){
        try {
            query = "SELECT PATIENT_NAME, AGE, SEX, ADDRESS, TO_CHAR(ADMISSION_DATE, '" + DateFormat + "') AS ADMISSION_DATE FROM BASIC_INFO WHERE PATIENT_ID = " + ID.toString();
            ResultSet rs = statement.executeQuery(query);
            String LineToWrite = "";

            if (rs.next()) {
                // PATIENT_NAME, AGE, SEX, ADDRESS, ADMISSION_DATE
                String Name = rs.getString("PATIENT_NAME");
                String Age = rs.getString("AGE");
                String Sex = rs.getString("SEX");
                String Address = rs.getString("ADDRESS");
                String AdmissionDate = rs.getString("ADMISSION_DATE");

                LineToWrite += Name + WordSeparator;
                LineToWrite += Age + WordSeparator;
                LineToWrite += Sex + WordSeparator;
                LineToWrite += Address + WordSeparator;
                LineToWrite += AdmissionDate + WordSeparator;
                LineToWrite += PhoneNumbers;

                return B_FileSystem.B_FileSystem_instance.WriteToFile(LineToWrite, ContactFileName, true);
            }
        } catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Method for extracting phone numbers from files for patients.
     * Should be called only after database has been populated with records.
     */
    public boolean GetPhoneNumbersFromFile(){
        try {
            if (B_FileSystem.B_FileSystem_instance.SetUpFileReader(ContactFileName, WordSeparator)){
                String Name, Age, Sex, Address, AdmissionDate;
                Integer ID = 0;
                ArrayList<String> PhoneNumbers;

                while(true){
                    Name = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                    if (Name == null) break;
                    B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                    Age = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                    B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                    Sex = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                    B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                    Address = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                    B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                    AdmissionDate = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                    B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                    PhoneNumbers = new ArrayList<>();
                    while(true){
                        String ReadNumber = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                        if (ReadNumber.contains(LineSeparator)) break;
                        PhoneNumbers.add(ReadNumber);
                    }

                    query = "SELECT PATIENT_ID FROM BASIC_INFO " +
                            "WHERE PATIENT_NAME = '" + Name + "' AND " +
                            "AGE = " + Age + " AND " +
                            "SEX = '" + Sex + "' AND " +
                            "ADDRESS = '" + Address + "' AND " +
                            "ADMISSION_DATE = TO_DATE('" + AdmissionDate + "', '" + DateFormat + "')";

                    System.out.println(query);
                    ResultSet rs = statement.executeQuery(query);
                    if (rs.next()){
                        ID = rs.getInt("PATIENT_ID");
                        InsertPhoneNumbers(ID, PhoneNumbers, false);
                    }
                }
                return true;
            }  else System.out.println("GetPhoneNumbersFromFile - " + "Couldn't setup reader.");
        } catch (SQLException | Defined_Exceptions e){
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Method for deleting phone number from file.
     * @return true if successful, false otherwise.
     */
    public boolean DeletePhoneNumberFromFile(){

        return true;
    }


    /**
     * Method for handling information updating relating to a single ID.
     * @param ID the patient ID whose information is to be updated.
     * @param Columns the column information to be updated.
     * @throws Defined_Exceptions when null or no column information has been given.
     */
    public boolean UpdateInformation(Integer ID, ArrayList<E_ColumnInfo> Columns) throws Defined_Exceptions{
        // Check if columns is null
        if (Columns == null)          throw new Defined_Exceptions("NULL_COLUMN_ARRAY");
        // No column information given exception
        else if (Columns.size() == 0) throw new Defined_Exceptions("ZERO_COLUMNS_FOR_RECORD");

        query = "UPDATE BASIC_INFO SET ";
        LoopThrough(Columns, 0, 5, ID);

        if (HandleUpdateExecution()){
            query = "UPDATE IN_DEPTH_INFO SET ";
            LoopThrough(Columns, 5, Columns.size(), ID);
            if(HandleUpdateExecution()){
                return UpdateRecordFile(Columns, true, Columns.get(0).ColumnValue);
            }
        }

        return false;
    }


    /**
     * Method for updating the record file after modification.
     * @param Columns the column information to be updated.
     * @return
     */
    public boolean UpdateRecordFile(ArrayList<E_ColumnInfo> Columns, boolean Type, String Name){
        // Setup file reader and continue only if successful.
        if (B_FileSystem.B_FileSystem_instance.SetUpFileReader(RecordFileName, WordSeparator)) {
            String TempFile = "Temp.txt", Fragment = null, TempLine = null;
            Boolean Append = false;

            while (true){
                TempLine = "";
                Fragment = null;

                // Read line from record file.
                while(true) {
                    Fragment = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                    if (Fragment == null) break;
                    B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();
                    if (Fragment.contains(LineSeparator)){
                        Fragment = LineSeparator + "\n" + WordSeparator;
                        TempLine += Fragment;
                        break;
                    }
                    TempLine += Fragment + "#";
                }

                if (Fragment == null)   break;
                else if (TempLine.toLowerCase().contains(Name.toLowerCase())) continue;

                Append = B_FileSystem.B_FileSystem_instance.WriteToFile(TempLine, TempFile, Append);
            }

            TempLine = "";
            if (Type) {
                for (int i = 0; i < Columns.size(); ++i) {
                    E_ColumnInfo columnInfo = Columns.get(i);
                    TempLine += columnInfo.ColumnName + WordSeparator + columnInfo.ColumnValue + WordSeparator + columnInfo.ColumnType + WordSeparator;
                }
                TempLine += LineSeparator + "\n" + WordSeparator;
                B_FileSystem.B_FileSystem_instance.WriteToFile(TempLine, TempFile, true);
            }

            // Write temp back to main file.
            if (B_FileSystem.B_FileSystem_instance.SetUpFileReader(TempFile)) {
                Append = false;
                do {
                    // Read from temp file.
                    TempLine = B_FileSystem.B_FileSystem_instance.ReadFromFileNextLine();

                    // If line read isn't null then write it back to column file.
                    if (TempLine != null)
                        if (TempLine.length() > 2)
                            Append = B_FileSystem.B_FileSystem_instance.WriteToFile(TempLine + "\n", RecordFileName, Append);
                        else
                            Append = B_FileSystem.B_FileSystem_instance.WriteToFile(TempLine, RecordFileName, Append);
                } while (TempLine != null);

                B_FileSystem.B_FileSystem_instance.WriteToFile("\n", TempFile, false);

                return true;
            } else System.out.println("UpdateRecordFile - Failed to setup file reader for temp");
        } else System.out.println("UpdateRecordFile - Failed to setup file reader for " + RecordFileName);

        return false;
    }


    /**
     * Helper method for UpdateInformation
     * @param ID the patient ID whose information is to be updated.
     * @param Columns the column information to be updated.
     * @param start the starting of the loop.
     * @param finish the ending of the loop.
     */
    private void LoopThrough(ArrayList<E_ColumnInfo> Columns, int start, int finish, Integer ID){
        E_ColumnInfo column;
        // Go through each column name and information and add it to the query statement.
        for(int i = start; i < finish; ++i){
            column = Columns.get(i);

            // Column Name = Column Value
            query += column.ColumnName + " = " + AppendBasedOnType(column.ColumnValue, column.ColumnType);

            if (i < finish-1)       query += ", ";
        }

        query += " WHERE PATIENT_ID = " + ID.toString();
    }


    /**
     * Method for deleting records from database.
     * @param ID the id of the record to be deleted.
     * @param Name the name of the record holder to be deleted.
     * @return true if successful. False otherwise.
     */
    public boolean DeleteRecordData(Integer ID, String Name){
        query = "DELETE FROM IN_DEPTH_INFO WHERE PATIENT_ID = " + ID.toString();
        if (HandleUpdateExecution()){
            query = "DELETE FROM BASIC_INFO WHERE PATIENT_ID = " + ID.toString();
            if (HandleUpdateExecution()) {
                return UpdateRecordFile(null, false, Name);
            }
        }
        return false;
    }


    /**
     * Method for getting information stored in the data base.
     * @param ID the id of the patient whose information is to be received. IF null, all record data is fetched.
     * @return result stored in resultset if successful, null otherwise.
     */
    public ResultSet GetInformation(Integer ID){
        query = "SELECT B.PATIENT_ID, B.PATIENT_NAME, B.AGE, B.SEX, B.ADDRESS, TO_CHAR(B.ADMISSION_DATE, '" + DateFormat + "') AS ADMISSION_DATE, ";
        for (int i = 0; i < ListOfColumns.size(); ++i) {
            query += "I." + ListOfColumns.get(i).ColumnName;

            if (i != ListOfColumns.size() - 1)
                query += ", ";
            else if (ID != null)
                query += " FROM BASIC_INFO B, IN_DEPTH_INFO I WHERE B.PATIENT_ID = I.PATIENT_ID AND B.PATIENT_ID = " + ID.toString() + " ORDER BY B.PATIENT_ID ASC";
            else
                query += " FROM BASIC_INFO B, IN_DEPTH_INFO I WHERE B.PATIENT_ID = I.PATIENT_ID ORDER BY B.PATIENT_ID ASC";
        }
        if (ResultSetHandler()) return resultSet;
        else                    return null;
    }




    public ResultSet SearchInformation(ArrayList<E_ColumnInfo> SearchParams){
        query = "SELECT * FROM " +
                "BASIC_INFO B, IN_DEPTH_INFO I " +
                "WHERE ";

        for(int i = 0; i < SearchParams.size(); ++i){
            E_ColumnInfo Param = SearchParams.get(i);
            query += Param.ColumnName + " = ";

            switch(Param.ColumnType){
                case 1:
                    query += "'" + Param.ColumnValue + "'";
                    break;
                case 2:
                    query += Param.ColumnValue;
                    break;
            }

            if (i != SearchParams.size()-1)     query += " AND ";
        }

        if (ResultSetHandler())
            return resultSet;

        return null;
    }


    /**
     * Method for getting the total number of records in the database.
     * @return the total number of records in the database if successful otherwise -1.
     */
    public Integer GetTotalRecordNumber(){
        query = "SELECT MAX(PATIENT_ID) AS MAX FROM BASIC_INFO";
        return IntegerHandler();
    }


    /**
     * Method for getting information stored in the data base.
     * @param ID the ID of the patient whose basic information is to be retrieved.
     * @return result stored in resultset if successful, null otherwise.
     */
    public ResultSet GetAddress(Integer ID){
        query = "SELECT ADDRESS FROM BASIC_INFO B WHERE B.PATIENT_ID = " + ID.toString();
        if (ResultSetHandler()) return resultSet;
        else                    return null;
    }


    /**
     * Used to execute statements directly read from file system.
     * @param Exec the statement to execute.
     * @param type if table type then 1, if record type then 2.
     * @return true if successful. False otherwise.
     */
    public boolean ExecuteStatement(String Exec, int type){
        try{
            System.out.println("From ExeQur: " + Exec);
            statement.execute(Exec);
            return true;
        } catch (SQLException e){
            e.printStackTrace();
        }

        return false;
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
     * Method for altering IN_DEPTH_INFO table to add new column.
     * @param ColumnName the name of the column to be added.
     * @param ColumnType the type of the column. 1-VARCHAR2, 2-NON-VARCHAR2, 3-DATE
     * @param ColumnSize the size of the VARCHAR2 column.
     * @param WriteCondition whether to write to the file or not.
     * @throws Defined_Exceptions when the VARCHAR2 is set as size 0.
     * @return true if successful, false otherwise.
     */
    public boolean AddColumn(String ColumnName, Integer ColumnType, Integer ColumnSize, boolean WriteCondition) throws Defined_Exceptions{
        // Zero varchar2 not allowed.
        if (ColumnType == 1 && ColumnSize == 0) throw new Defined_Exceptions("VARCHAR_SIZE_ZERO");

        query = "ALTER TABLE IN_DEPTH_INFO ADD " + ColumnName + " ";
        switch(ColumnType){
            case 1: // Column is of type VARCHAR2
                query += "VARCHAR2(" + ColumnSize.toString() + ")";
                break;
            case 2: // Column is of type non-VARCHAR2
                query += "NUMBER";
                break;
            case 3: // TODO Column is of type Date
                break;
        }

        // This is to write the column information back to the file
        if (ExecuteStatement(query, 0)) {
            if (WriteCondition) {
                if(B_FileSystem.B_FileSystem_instance.WriteToFile(ColumnName + WordSeparator + ColumnType + Seperator, ColumnFileName, true))
                    return true;
                else System.out.println("AddColumn - Writing to file failed!");
            } else return true;
        } else System.out.println("AddColumn - Column addition failed!");

        return false;
    }


    /**
     * Method for altering IN_DEPTH_INFO table to add new column with checking constraint for number type.
     * @param ColumnName the name of the column to be added.
     * @param ColumnType the type of the column. 1-VARCHAR2, 2-NON-VARCHAR2, 3-DATE
     * @param UpperBound the upper-bound of the values to consider.
     * @param LowerBound the lower-bound of the values to consider.
     * @param WriteCondition whether to write to fle or not.
     * @throws Defined_Exceptions when either of the following happens: Not number type, same upper and lower limit, same named column.
     * @return true if successful, false otherwise.
     */
    public boolean AddColumnWithCheck(String ColumnName, Integer ColumnType, Integer UpperBound, Integer LowerBound, boolean WriteCondition) throws Defined_Exceptions{
        // Column is not of number type
        if (ColumnType != 2)                throw new Defined_Exceptions("NOT_NUMBER");
        // Same value for upper and lower limit not allowed.
        else if (UpperBound == LowerBound)  throw new Defined_Exceptions("SINGLE_VALUE_BOUND");

        if (AddColumn(ColumnName, ColumnType, 0, false)) {
            query = "ALTER TABLE IN_DEPTH_INFO ADD CONSTRAINT CHK_IND_" + ColumnName + " CHECK " +
                    "(" + ColumnName + " >= " + LowerBound.toString() + " AND " + ColumnName + " <= " + UpperBound.toString() + ")";

            // This is to write the column information back to the file
            if (ExecuteStatement(query, 0))
                if (WriteCondition)
                    return B_FileSystem.B_FileSystem_instance.WriteToFile(ColumnName + WordSeparator + ColumnType + WordSeparator + LowerBound + WordSeparator + UpperBound + Seperator, ColumnFileName, true);
                return true;
        }

        return false;
    }


    /**
     * Method for altering IN_DEPTH_INFO table to add new column with checking constraint for VARCHAR2 type.
     * @param ColumnName the name of the column to be added.
     * @param ColumnType the type of the column. 1-VARCHAR2, 2-NON-VARCHAR2, 3-DATE
     * @param ColumnSize the size of the VARCHAR2 column.
     * @param DomainValues the domain of the column value.
     * @param WriteCondition whether to write to file or not.
     * @throws Defined_Exceptions if the columns parameters don't meet the requirements.
     * @return true if successful, false otherwise.
     */
    public boolean AddColumnWithCheck(String ColumnName, Integer ColumnType, Integer ColumnSize, ArrayList<String> DomainValues, boolean WriteCondition) throws Defined_Exceptions{
        // This method can only be used for VARCHAR2 type columns.
        if (ColumnType != 1)                throw new Defined_Exceptions("NOT_VARCHAR");
        // VARCHAR2 can't be of size 0.
        else if (ColumnSize == 0)           throw new Defined_Exceptions("VARCHAR_SIZE_ZERO");
        // Domain can't be null.
        else if (DomainValues == null)      throw new Defined_Exceptions("NULL_DOMAIN");
        // Domain can't be empty.
        else if (DomainValues.size() == 0)  throw new Defined_Exceptions("EMPTY_DOMAIN");

        if (AddColumn(ColumnName, ColumnType, ColumnSize, false)) {
            String domain = "";
            query = "ALTER TABLE IN_DEPTH_INFO ADD CONSTRAINT CHK_IND_" + ColumnName + " CHECK (" + ColumnName + " IN (";
            for (int i = 0; i < DomainValues.size(); ++i) {
                query += "'" + DomainValues.get(i) + "'";
                domain += WordSeparator + DomainValues.get(i);

                if (i != DomainValues.size() - 1) query += ", ";
                else query += "))";
            }

            // This is to write the column information back to the file
            if (ExecuteStatement(query, 0))
                if (WriteCondition){
                    if (B_FileSystem.B_FileSystem_instance.WriteToFile(ColumnName + WordSeparator + ColumnType + WordSeparator + ColumnSize + domain  + Seperator, ColumnFileName, true))
                        return true;
                    else System.out.println("AddColumnWithCheck - Failed to write to file!");
                } else return true;
            else System.out.println("AddColumnWithCheck - Constraint addition failed!");
        } else System.out.println("AddColumnWithCheck - Column addition failed!");

        return false;
    }


    /**
     * Method for dropping column from table.
     * @param ColumnName the name of the column to be dropped.
     * @return true if successful, false otherwise.
     */
    public boolean DropColumn(String ColumnName){
        query = "ALTER TABLE IN_DEPTH_INFO DROP COLUMN " + ColumnName;
        boolean con = ExecuteStatement(query, 0);

        if (con){
            // If dropping column was successful then update the column list file.
            if (UpdateColumnList(ColumnName) == false)
                System.out.println("DropColumn - Failed to update column file");
        } else
            System.out.println("DropColumn - Failed to drop " + ColumnName);

        return con;
    }


    /**
     * Method for dropping columns with check constraints.
     * @param ColumnName the nme of the column to be dropped.
     * @return true if successful, false otherwise.
     */
    public boolean DropColumnWithConstraint(String ColumnName){
        query = "ALTER TABLE IN_DEPTH_INFO DROP CONSTRAINT CHK_IND_" + ColumnName;
        if (ExecuteStatement(query, 0)){
            if(DropColumn(ColumnName)) return true;
            else System.out.println("Failed to drop constraint!!!!");
        } else {
            System.out.println("Failed to drop column!");
        }
        return false;
    }


    /**
     * Method for updating the column list file.
     * @return true if successful. False otherwise.
     */
    private boolean UpdateColumnList(String ColumnName){
        // Setup file reader and continue only if successful.
        if (B_FileSystem.B_FileSystem_instance.SetUpFileReader(ColumnFileName, WordSeparator)) {
            String TempFile = "Temp.txt", Fragment = null, TempLine = null;
            Boolean Append = false;

            while (true){
                TempLine = "";
                Fragment = null;

                while(true) {
                    // Read line from column file.
                    Fragment = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                    if (Fragment == null) break;
                    B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();
                    if (Fragment.contains(LineSeparator)){
                        Fragment = LineSeparator + '\n' + WordSeparator;
                        TempLine += Fragment;
                        break;
                    }
                    TempLine += Fragment + WordSeparator;
                }

                if (Fragment == null)   break;
                else if (TempLine.contains(ColumnName)) continue;

                Append = B_FileSystem.B_FileSystem_instance.WriteToFile(TempLine, TempFile, Append);
            }

            if (B_FileSystem.B_FileSystem_instance.SetUpFileReader(TempFile)) {
                Append = false;
                do {
                    // Read from temp file.
                    TempLine = B_FileSystem.B_FileSystem_instance.ReadFromFileNextLine();

                    // If line read isn't null then write it back to column file.
                    if (TempLine != null)
                        if (TempLine.length() > 2)
                            Append = B_FileSystem.B_FileSystem_instance.WriteToFile(TempLine + "\n", ColumnFileName, Append);
                        else
                            Append = B_FileSystem.B_FileSystem_instance.WriteToFile(TempLine, ColumnFileName, Append);
                } while (TempLine != null);

                return true;
            } else System.out.println("UpdateColumnList - Failed to setup file reader for temp");
        } else System.out.println("UpdateColumnList - Failed to setup file reader for columnName");

        return false;
    }


    /**
     * Method for getting the total number of columns in the table.
     * @return number if successful otherwise null.
     */
    public Integer GetTotalColumnNumber(){
        // COLUMN_NAME FROM USER_TAB_COLUMNS WHERE TABLE_NAME='IN_DEPTH_INFO'
        query = "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='IN_DEPTH_INFO'";
        try {
            ResultSet rs = statement.executeQuery(query);
            if (rs != null) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
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
     * Handles execution of statement and produces a result set. Primarily used to retrieve data.
     * @return true if successful execution. false otherwise.
     */
    private Integer IntegerHandler(){
        try{
            resultSet = statement.executeQuery(query);
            resultSet.next();
            return resultSet.getInt("MAX");
        } catch (SQLException e){
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * Handles execution of statement and produces a integer. Primarily used to update data.
     * @return true if integer produced is non-zero. false otherwise.
     */
    private boolean HandleUpdateExecution(){
        try{
            System.out.println(query);
            if (statement.executeUpdate(query) > 0) return true;
        } catch (SQLException e){
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Handles direct statement execution. Used to execute statement read from the file.
     * @param Query the statement to execute.
     * @return true if successful. False otherwise.
     */
    public boolean HandleUpdateExecution(String Query){
        try{
            if (statement.executeUpdate(Query) > 0) return true;
        } catch (SQLException e){
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Builds the insert query that will be executed.
     * @param Columns the column information to be inserted.
     */
    private void InsertQueryBuilder(ArrayList<E_ColumnInfo> Columns){
        E_ColumnInfo column;

        for(int i = 0; i < Columns.size(); ++i){
            column = Columns.get(i);
            query += column.ColumnName;
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
     * @param type the type of the column. 1-> VARCHAR2, 2->NUMBER, 3->DATE.
     * @return the string to be appended.
     */
    private String AppendBasedOnType(String Value, int type){
        switch(type){
            case 1: // Column type is VARCHAR2. Means we need quotations.
                return "'" + Value + "'";
            case 2: // Column type is number. Means we do not need quotations.
                return Value;
            case 3: // Column type is date. Means we need proper formatting.
                return "TO_DATE('" + Value + "', '" + DateFormat + "')";
            default:
                return Value;
        }
    }
}

/*
DROP USER ishrak CASCADE;
CREATE USER ishrak IDENTIFIED BY dragonsword05;
GRANT CONNECT, RESOURCE, CREATE SESSION, DBA TO ishrak;
 */
