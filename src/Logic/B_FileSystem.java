package Logic;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * <h> This class handles all the operations related to file IO </h>
 * <p>
 *     The main purpose of this class is to write and read things from and to files.
 *     It writes SQL commands, patient information.
 * </p>
 *
 * @author Shakleen Ishfar
 * @version 1.0.0.0
 * @since 05 November, 2018
 */
public class B_FileSystem {
    private Scanner fileReader;
    private FileWriter fileWriter;
    public static B_FileSystem B_FileSystem_instance = new B_FileSystem();


    /**
     * Constructor. Private constructor means that only one instance
     * of this class can exist at any given time.
     */
    private B_FileSystem(){}


    /**
     * Method for writing a single line to a proper file.
     * @param Line the line that is to be writen to file.
     * @param FileName the file where the line should be written.
     * @param Append whether to append to existing data (true) or clear out and start fresh (false).
     */
    public void WriteToFile(String Line, String FileName, boolean Append){
        try{
            fileWriter = new FileWriter(FileName, Append);
            fileWriter.write(Line);
        } catch(IOException e){
            e.printStackTrace();
        } finally {
            if (fileWriter != null){
                CloseFileWriter();
            }
        }
    }


    /**
     * Method to read from file.
     * @param FileName the file where we want to read from.
     * @return the next line from the file name specified or null if any issue occured.
     */
    public String ReadFromFile(String FileName){
        try {
            System.out.println(FileName);
            if (fileReader == null) fileReader = new Scanner(new FileReader(FileName));

            if (fileReader.hasNext()) return fileReader.nextLine();
            else                      return null;
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Used to update the information stored in the database from the sql file.
     * Used only when the application had data stored before an unexpected failure.
     */
    public void UpdateDatabase(){
        String exec;
        while(true){
            exec = B_FileSystem.B_FileSystem_instance.ReadFromFile("SQL_Statements.sql");

            if (exec == null)   break;
            else                B_Database.B_database_instance.ExecuteStatement(exec);
        }
    }


    /**
     * Used to close File writer with proper exception handling.
     */
    private void CloseFileWriter(){
        try{
            fileWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
