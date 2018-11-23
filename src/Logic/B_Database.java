package Logic;

import java.io.File;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * <h> Class for handling data base portion of the application </h>
 * <p>This is the main backend of the application. It handles the storage of data. The insertion, deletion and search is extremely efficient
 * in the database as compared to file system. That is why the application mainly depends on this class to retrieve and manipulate data.
 * A backup of the data is kept in the file system in case of failure. But main interaction is done using this class. The class mainly deals
 * with 2 types of interactions. Interaction with Table structure i.e. with columns and interaction with records. Whenever new data is added
 * or old data is manipulated (Updated or deleted) appropriate file systems are also modified to reflect the manipulation.</p>
 * @author Shakleen Ishfar
 * @version 1.0.0.0
 * @since 05/11/2018
 */
public class B_Database {
    private static Connection connection;                                           // Connection type object to get connection using driver.
    private static Statement statement;                                             // Statement type object to execute DDL and DML queries.
    private static String query;                                                    // Holds query for execution by statement object.
    private static String values;                                                   // Used to store column values.
    private static ResultSet resultSet;                                             // ResultSet type object to return database tables.
    private static final String ClassName = "oracle.jdbc.OracleDriver";             // The class name to search in OJDBC.jar
    private static final String dbURL = "jdbc:oracle:thin:@localhost:1521:xe";      // The url of the database.
    private static final String WordSeparator = "#";                                // Separates two attribute values in the file.
    private static final String LineSeparator = "|";                                // Separates two different record data in file.
    private static final String Separator =
            WordSeparator + LineSeparator + '\n' + WordSeparator;
    private static final String DateFormat = "YYYY/MM/DD";                          // The universal data format that is used in the dbms.
    public static String ColumnFileName;                                            // Name of the file where column information is kept.
    public static String RecordFileName;                                            // Name of the file where column information is kept.
    public static String ContactFileName;                                           // Name of the file where column information is kept.
    public static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");                              // The universal data format that is used in the dbms.
    public static ArrayList<E_ColumnInfo> ListOfColumns;                            // An array list that keeps all the names of the columns.

    /**
     * Constructor. Initializes connection variable to be null.
     * The constructor is private. Meaning another instance of this class can never be created.
     */
    private B_Database (){
        connection = null;
    }

    /**
     * Method for logging in.
     * @param Name User name
     * @param Password password
     * @return true if successful, false otherwise.
     */
    public static boolean LogIn(String Name, String Password){
        try {
            Class.forName(ClassName);                                               // Get the appropriate class pointed to by ClassName String.
            connection = DriverManager.getConnection(dbURL, Name, Password);        // Create a connection using the passed Name and Password.
            if(connection != null){                                                 // If connection succeeds then do the following.
                statement = connection.createStatement();                           // Create a statement for executing queries and updates.
                ColumnFileName = Name + "ColumnNames.txt";                          // Set the column file name for the user.
                RecordFileName = Name + "RecordData.txt";                           // Set the record file name for the user.
                ContactFileName = Name + "RecordContactData.txt";                   // Set the contact file name for the user.
                SetupDatabaseForUsage();                                            // Setup the database for usage.
                return true;                                                        // Return true if everything up-to now has succeeded.
            }
        }
        catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Method checks the status of the database. If the database has no table structure. then it creates them from scratch.
     *
     * @return true if successful. false otherwise.
     */
    private static void SetupDatabaseForUsage(){
        try{
            /*
             * This portion checks the database for the table structure.
             * First it searches the database for the BASIC_INFO Table.
             * Second, it searches for the database for the IN_DEPTH_INFO table.
             * If they don'y exist they are created before proceeding.
             */
            boolean BASIC_INFO, IN_DEPTH_INFO;
            ResultSet rs = statement.executeQuery("SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE 'BASIC_INFO'");
            BASIC_INFO = rs.next();
            rs = statement.executeQuery("SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE 'IN_DEPTH_INFO'");
            IN_DEPTH_INFO = rs.next();

            // Checks if the appropriate files exists for creating the table structure and record restoration.
            CheckAndDownload();

            // Initializes the ArrayList where we shall store the column names for future usage.
            ListOfColumns = new ArrayList<>();

            // Fetches the column information from the file which contains column information.
            GetColumnInfoFromFile();

            if (BASIC_INFO == false && IN_DEPTH_INFO == false)
                BASIC_INFO = IN_DEPTH_INFO = ReconstructDatabaseFromFiles();
        }
        catch (SQLException de){

        }
    }


    /**
     * Method checks the existence of the files necessary for the application to work.
     * When it doesn't find the files it will check the online backup for the files.
     */
    private static void CheckAndDownload(){
        if (!new File(ColumnFileName).exists()) B_Network.DownloadFromDrive(ColumnFileName);
        if (!new File(RecordFileName).exists()) B_Network.DownloadFromDrive(RecordFileName);
        if (!new File(ContactFileName).exists()) B_Network.DownloadFromDrive(ContactFileName);
    }


    /**
     * Method for reconstructing the database and repopulating it with information stored in the files.
     * Is called when the oracle database had faced an error and had to be reinstalled or somehow deleted the users
     * information.
     * @return true if successful, false otherwise.
     */
    private static boolean ReconstructDatabaseFromFiles(){
        /*
         * The method does the following steps. If a step fails it does not move onto the next step.
         * Step 1: It creates the table structure.
         * Step 2: It creates the columns with the information of the columns stored in the column file.
         * Step 3: It inserts the records into the table from the information stored in the record file.
         * Step 4: It inserts the contact of the records into the table.
         */
        try {
            if (CreateTableStructure()) {
                if (SetupTableColumnsFromFile()) {
                    if (RepopulateDatabaseWithRecords())
                        return GetPhoneNumbersFromFile();
                    System.out.println("ReconstructDatabaseFromFiles - Couldn't add records to table!");
                } else System.out.println("ReconstructDatabaseFromFiles - Couldn't create columns!");
            } else System.out.println("ReconstructDatabaseFromFiles - Couldn't execute table creation statements!");
        }
        catch (Defined_Exceptions e){

        }
        return false;
    }


    /**
     * Method handles repopulating the table with appropriate columns.
     * It fetches column information from the column file and inserts them into the dbms table.
     * @return true if successful, false otherwise.
     */
    private static boolean SetupTableColumnsFromFile() throws Defined_Exceptions{
        E_ColumnInfo column;
        int n = ListOfColumns.size();

        for(int i = 0; i < n; ++i){
            column = ListOfColumns.get(i);

            // Column type 1 means that the column is of String (VARCHAR2) type.
            if (column.ColumnType == 1) AddColumnWithCheck(column.ColumnName, column.ColumnType, column.ColumnSize, column.DomainValues, false);

            // Column type 2 means that the column is of Numerical (NUMBER) type.
            else AddColumnWithCheck(column.ColumnName, column.ColumnType, column.UpperLimit, column.LowerLimit, false);
        }

        return true;
    }


    /**
     * Method for retrieving column related information from file and storing them into a list.
     * @return true if successful, false otherwise.
     */
    public static boolean GetColumnInfoFromFile(){
        String ColumnName = null, LineRead = null;
        Integer ColumnType = null, ColumnSize = null;
        ArrayList<String> DomainValues = null;
        boolean EndOfLine = false;

        if (B_FileSystem.B_FileSystem_instance.SetUpFileReader(ColumnFileName, WordSeparator)) {
            ListOfColumns.clear();

            while (true) {
                // Getting name
                ColumnName = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                if (ColumnName == null) break;
                B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                // Getting type
                ColumnType = B_FileSystem.B_FileSystem_instance.ReadFromFileNextInt();
                B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                // Taking appropriate action based on type.
                if (ColumnType == 1){
                    // Get column size for VARCHAR2
                    ColumnSize = B_FileSystem.B_FileSystem_instance.ReadFromFileNextInt();
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
                }
                else {
                    Integer LowerLimit = B_FileSystem.B_FileSystem_instance.ReadFromFileNextInt();
                    B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();
                    Integer UpperLimit = B_FileSystem.B_FileSystem_instance.ReadFromFileNextInt();
                    B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();
                    B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                    B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();
                    ListOfColumns.add(new E_ColumnInfo(ColumnName, ColumnType, true, UpperLimit, LowerLimit));
                }
            }

            return true;
        }

        return false;
    }


    /**
     * Method for creating table structure. Creates 3 tables. BASIC_INFO, IN_DEPTH_INFO and PATIENT_PHONE_NUMBERS.
     * @return true when success otherwise false.
     */
    private static boolean CreateTableStructure(){
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

        if (ExecuteStatement()){
            query = "CREATE SEQUENCE SEQ_PATIENT_ID MINVALUE 1 START WITH 1 INCREMENT BY 1 CACHE 10";

            if (ExecuteStatement()){
                query = "CREATE TABLE PATIENT_PHONE_NUMBERS(" +
                            "PATIENT_ID NUMBER," +
                            "PHONE_NUMBER VARCHAR2(20)," +
                            "CONSTRAINT FK_PATIENT_ID FOREIGN KEY (PATIENT_ID) REFERENCES BASIC_INFO" +
                        ")";

                if (ExecuteStatement()){
                    query = "CREATE TABLE IN_DEPTH_INFO(" +
                                "PATIENT_ID NUMBER," +
                                "CONSTRAINT FK_IND_PATIENT_ID FOREIGN KEY (PATIENT_ID) REFERENCES BASIC_INFO (PATIENT_ID)" +
                            ")";

                    return ExecuteStatement();
                }
            }
        }

        return false;
    }


    /**
     * Method for repopulating DBMS with the records from file.
     * @return true if successful, false otherwise.
     */
    private static boolean RepopulateDatabaseWithRecords(){
        if (B_FileSystem.B_FileSystem_instance.SetUpFileReader(RecordFileName, WordSeparator)){
            String Name = null, Value = null;
            Integer Type = null;
            Boolean Break = false;
            ArrayList<E_ColumnInfo> BasicColumnValues, InDepthColumnValues;

            while(true) {
                BasicColumnValues = new ArrayList<>();
                InDepthColumnValues = new ArrayList<>();

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
    public static boolean InsertInformation(ArrayList<E_ColumnInfo> BasicColumns, ArrayList<E_ColumnInfo> InDepthColumns, boolean WrtieCondition){
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
    private static boolean InsertBasicInformation(ArrayList<E_ColumnInfo> Columns, boolean Type) throws Defined_Exceptions{
        // Check to see if columns is null.
        if (Columns == null)            throw new Defined_Exceptions("NULL_COLUMN_ARRAY");
        // No column information given exception
        else if (Columns.size() == 0)   throw new Defined_Exceptions("ZERO_COLUMNS_FOR_RECORD");

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
    public static boolean InsertPhoneNumbers(Integer ID, ArrayList<String> PhoneNumbers, boolean WriteCondition) throws Defined_Exceptions{
        // Check if null
        if (PhoneNumbers == null)               throw new Defined_Exceptions("NULL_COLUMN_ARRAY");
        // Check if zero elements
        else if (PhoneNumbers.size() == 0)      throw new Defined_Exceptions("ZERO_COLUMNS_FOR_RECORD");

        boolean con = false;
        int n = PhoneNumbers.size();
        String LineToWrite = "";
        for (int i = 0; i < n; ++i) {
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
    public static boolean InsertPhoneNumbers(ArrayList<E_ColumnInfo> Basic_Info, ArrayList<String> PhoneNumbers, boolean WriteCondition){
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

            ResultSetHandler();

            if (resultSet.next()){
                ID = resultSet.getInt("PATIENT_ID");
                if (ID >= 1) return InsertPhoneNumbers(ID, PhoneNumbers, WriteCondition);
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
    private static boolean WritePhoneNumbersToFile(Integer ID, String PhoneNumbers){
        try {
            query = "SELECT PATIENT_NAME, AGE, SEX, ADDRESS, TO_CHAR(ADMISSION_DATE, '" + DateFormat + "') AS ADMISSION_DATE FROM BASIC_INFO WHERE PATIENT_ID = " + ID.toString();
            ResultSetHandler();
            String LineToWrite = "";

            if (resultSet.next()) {
                // PATIENT_NAME, AGE, SEX, ADDRESS, ADMISSION_DATE
                String Name = resultSet.getString("PATIENT_NAME");
                String Age = resultSet.getString("AGE");
                String Sex = resultSet.getString("SEX");
                String Address = resultSet.getString("ADDRESS");
                String AdmissionDate = resultSet.getString("ADMISSION_DATE");

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
    public static boolean GetPhoneNumbersFromFile(){
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

                    ResultSetHandler();
                    if (resultSet.next()){
                        ID = resultSet.getInt("PATIENT_ID");
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
    public static boolean DeletePhoneNumberFromFile(String Name, String Age, String Sex, String Address, String AdmissionDate){
        if (B_FileSystem.B_FileSystem_instance.SetUpFileReader(ContactFileName, WordSeparator)){
            String ReadName, ReadAge, ReadSex, ReadAddress, ReadAdmissionDate;
            boolean Append = false;

            while(true){
                ReadName = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                if (ReadName == null)   break;
                B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                ReadAge = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                ReadSex = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                ReadAddress = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                ReadAdmissionDate = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                String LineToWrite = ReadName + WordSeparator + ReadAge + WordSeparator + ReadSex + WordSeparator +
                        ReadAddress + WordSeparator + ReadAdmissionDate + WordSeparator, PhoneNo = "";

                while (true){
                    PhoneNo = B_FileSystem.B_FileSystem_instance.ReadFromFileNext();
                    B_FileSystem.B_FileSystem_instance.FileReaderSkipDelimeter();

                    if (PhoneNo.contains(LineSeparator)){
                        LineToWrite += LineSeparator + '\n' + WordSeparator;
                        break;
                    }
                    else {
                        LineToWrite += PhoneNo + WordSeparator;
                    }
                }

                boolean WriteCondition = ReadName.equals(Name) && ReadAge.equals(Age) && ReadSex.equals(Sex) &&
                        ReadAddress.equals(Address) && ReadAdmissionDate.equals(AdmissionDate);

                if (!WriteCondition) Append = B_FileSystem.B_FileSystem_instance.WriteToFile(LineToWrite, "Temp.txt", Append);
            }

            return WriteFromTempFile(ContactFileName);
        }

        return false;
    }


    /**
     * Method for handling information updating relating to a single ID.
     * @param ID the patient ID whose information is to be updated.
     * @param Columns the column information to be updated.
     * @throws Defined_Exceptions when null or no column information has been given.
     */
    public static boolean UpdateInformation(Integer ID, ArrayList<E_ColumnInfo> Columns) throws Defined_Exceptions{
        // Check if columns is null
        if (Columns == null)          throw new Defined_Exceptions("NULL_COLUMN_ARRAY");
        // No column information given exception
        else if (Columns.size() == 0) throw new Defined_Exceptions("ZERO_COLUMNS_FOR_RECORD");

        query = "UPDATE BASIC_INFO SET ";
        LoopThrough(Columns, 0, 5, ID);

        if (HandleUpdateExecution()){
            query = "UPDATE IN_DEPTH_INFO SET ";
            LoopThrough(Columns, 5, Columns.size(), ID);
            if(HandleUpdateExecution())
                return UpdateRecordFile(Columns, true, Columns.get(0).ColumnValue);
        }

        return false;
    }


    /**
     * Method for updating the record file after modification.
     * @param Columns the column information to be updated.
     * @return
     */
    public static boolean UpdateRecordFile(ArrayList<E_ColumnInfo> Columns, boolean Type, String Name){
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

            return WriteFromTempFile(RecordFileName);
        } else System.out.println("UpdateRecordFile - Failed to setup file reader for " + RecordFileName);

        return false;
    }


    /**
     * Method for writing back from temp file.
     * @return true if successful, false otherwise.
     */
    private static boolean WriteFromTempFile(String FileToWriteTo){
        String TempFile = "Temp.txt", TempLine = "";
        boolean Append;

        // Write temp back to main file.
        if (B_FileSystem.B_FileSystem_instance.SetUpFileReader(TempFile)) {
            Append = false;
            do {
                // Read from temp file.
                TempLine = B_FileSystem.B_FileSystem_instance.ReadFromFileNextLine();

                // If line read isn't null then write it back to column file.
                if (TempLine != null)
                    if (TempLine.length() > 2)
                        Append = B_FileSystem.B_FileSystem_instance.WriteToFile(TempLine + "\n", FileToWriteTo, Append);
                    else
                        Append = B_FileSystem.B_FileSystem_instance.WriteToFile(TempLine, FileToWriteTo, Append);
            } while (TempLine != null);

            B_FileSystem.B_FileSystem_instance.WriteToFile("\n", TempFile, false);

            return true;
        } else System.out.println("UpdateRecordFile - Failed to setup file reader for temp");

        return false;
    }


    /**
     * Helper method for UpdateInformation
     * @param ID the patient ID whose information is to be updated.
     * @param Columns the column information to be updated.
     * @param start the starting of the loop.
     * @param finish the ending of the loop.
     */
    private static void LoopThrough(ArrayList<E_ColumnInfo> Columns, int start, int finish, Integer ID){
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
    public static boolean DeleteRecordData(Integer ID, String Name){
        query = "DELETE FROM IN_DEPTH_INFO WHERE PATIENT_ID = " + ID.toString();
        if (HandleUpdateExecution()){
            query = "DELETE FROM PATIENT_PHONE_NUMBERS WHERE PATIENT_ID = " + ID.toString();
            if (HandleUpdateExecution()) {
                query = "SELECT PATIENT_NAME, AGE, SEX, ADDRESS, TO_CHAR(ADMISSION_DATE, '"
                        + DateFormat + "') AS ADMISSION_DATE FROM BASIC_INFO WHERE PATIENT_ID = " + ID.toString();

                if (ResultSetHandler()){
                    try{
                        if (resultSet != null){
                            while(resultSet.next()){
                                DeletePhoneNumberFromFile(
                                        resultSet.getString("PATIENT_NAME"),
                                        resultSet.getString("AGE"),
                                        resultSet.getString("SEX"),
                                        resultSet.getString("ADDRESS"),
                                        resultSet.getString("ADMISSION_DATE")
                                );
                            }
                        }
                    }
                    catch (SQLException e){
                        e.printStackTrace();
                    }
                }

                query = "DELETE FROM BASIC_INFO WHERE PATIENT_ID = " + ID.toString();
                if (HandleUpdateExecution()) {
                    return UpdateRecordFile(null, false, Name);
                }
            }
        }
        return false;
    }


    /**
     * Method for getting information stored in the data base.
     * @param ID the id of the patient whose information is to be received. IF null, all record data is fetched.
     * @return result stored in resultset if successful, null otherwise.
     */
    public static ResultSet GetInformation(Integer ID){
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


    /**
     * Method for searching information.
     * @param Name Name of the patient
     * @param Age Age of the patient
     * @param Sex sex of the patient
     * @param Address address of the patient
     * @return ResultSet containing information.
     */
    public static ResultSet SearchInformation(String Name, String Age, String Sex, String Address){
        query = "SELECT B.PATIENT_ID, B.PATIENT_NAME, B.AGE, B.SEX, B.ADDRESS, TO_CHAR(B.ADMISSION_DATE, '" + DateFormat + "') AS ADMISSION_DATE, ";
        for (int i = 0; i < ListOfColumns.size(); ++i) {
            query += "I." + ListOfColumns.get(i).ColumnName;

            if (i != ListOfColumns.size() - 1)
                query += ", ";
            else
                query += " FROM BASIC_INFO B, IN_DEPTH_INFO I WHERE B.PATIENT_ID = I.PATIENT_ID AND ";
        }

        int c = 0;
        if (Name != null){
            query += " B.PATIENT_NAME LIKE '%" + Name + "%'";
            ++c;
        }

        if (Age != null){
            if (c > 0)  query += " AND";
            query += " B.AGE = " + Age;
            ++c;
        }

        if (Sex != null){
            if (c > 0)  query += " AND";
            query += " B.SEX = '" + Sex + "'";
            ++c;
        }

        if (Address != null){
            if (c > 0)  query += " AND";
            query += " B.ADDRESS LIKE '%" + Address + "%'";
        }

        query += " ORDER BY B.PATIENT_ID ASC";

        if (ResultSetHandler())
            return resultSet;

        return null;
    }


    /**
     * Method for getting the total number of records in the database.
     * @return the total number of records in the database if successful otherwise -1.
     */
    public static Integer GetTotalRecordNumber(){
        query = "SELECT MAX(PATIENT_ID) AS MAX FROM BASIC_INFO";
        return IntegerHandler();
    }


    /**
     * Method for getting information stored in the data base.
     * @param ID the ID of the patient whose basic information is to be retrieved.
     * @return result stored in resultset if successful, null otherwise.
     */
    public static ResultSet GetAddress(Integer ID){
        query = "SELECT ADDRESS FROM BASIC_INFO B WHERE B.PATIENT_ID = " + ID.toString();
        if (ResultSetHandler()) return resultSet;
        else                    return null;
    }


    /**
     * Used to execute statements directly read from file system.
     * @return true if successful. False otherwise.
     */
    public static boolean ExecuteStatement(){
        try{
            System.out.println("From ExeQur: " + query);
            statement.execute(query);
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
    public static ResultSet GetPhoneNumbers(Integer ID){
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
    public static boolean AddColumn(String ColumnName, Integer ColumnType, Integer ColumnSize, boolean WriteCondition) throws Defined_Exceptions{
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
        if (ExecuteStatement()) {
            if (WriteCondition) {
                if(B_FileSystem.B_FileSystem_instance.WriteToFile(ColumnName + WordSeparator + ColumnType + Separator, ColumnFileName, true))
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
    public static boolean AddColumnWithCheck(String ColumnName, Integer ColumnType, Integer UpperBound, Integer LowerBound, boolean WriteCondition) throws Defined_Exceptions{
        // Column is not of number type
        if (ColumnType != 2)                throw new Defined_Exceptions("NOT_NUMBER");
        // Same value for upper and lower limit not allowed.
        else if (UpperBound == LowerBound)  throw new Defined_Exceptions("SINGLE_VALUE_BOUND");

        if (AddColumn(ColumnName, ColumnType, 0, false)) {
            query = "ALTER TABLE IN_DEPTH_INFO ADD CONSTRAINT CHK_IND_" + ColumnName + " CHECK " +
                    "(" + ColumnName + " >= " + LowerBound.toString() + " AND " + ColumnName + " <= " + UpperBound.toString() + ")";

            // This is to write the column information back to the file
            if (ExecuteStatement())
                if (WriteCondition)
                    return B_FileSystem.B_FileSystem_instance.WriteToFile(ColumnName + WordSeparator + ColumnType + WordSeparator + LowerBound + WordSeparator + UpperBound + Separator, ColumnFileName, true);
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
    public static boolean AddColumnWithCheck(String ColumnName, Integer ColumnType, Integer ColumnSize, ArrayList<String> DomainValues, boolean WriteCondition) throws Defined_Exceptions{
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
            if (ExecuteStatement())
                if (WriteCondition){
                    if (B_FileSystem.B_FileSystem_instance.WriteToFile(ColumnName + WordSeparator + ColumnType + WordSeparator + ColumnSize + domain  + Separator, ColumnFileName, true))
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
    public static boolean DropColumn(String ColumnName){
        query = "ALTER TABLE IN_DEPTH_INFO DROP COLUMN " + ColumnName;
        boolean con = ExecuteStatement();

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
    public static boolean DropColumnWithConstraint(String ColumnName){
        query = "ALTER TABLE IN_DEPTH_INFO DROP CONSTRAINT CHK_IND_" + ColumnName;
        if (ExecuteStatement()){
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
    private static boolean UpdateColumnList(String ColumnName){
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

            return WriteFromTempFile(ColumnFileName);
        } else System.out.println("UpdateColumnList - Failed to setup file reader for columnName");

        return false;
    }


    /**
     * Method for getting the total number of columns in the table.
     * @return number if successful otherwise null.
     */
    public static Integer GetTotalColumnNumber(){
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
    private static boolean ResultSetHandler(){
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
    private static Integer IntegerHandler(){
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
    private static boolean HandleUpdateExecution(){
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
    public static boolean HandleUpdateExecution(String Query){
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
    private static void InsertQueryBuilder(ArrayList<E_ColumnInfo> Columns){
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
    private static String AppendBasedOnType(String Value, int type){
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
