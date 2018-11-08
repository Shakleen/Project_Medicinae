package Logic;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
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
     * Used to set up File Reader.
     * @param FileName the name of the file to read from.
     * @return true if successful. False otherwise.
     */
    public boolean SetUpFileReader(String FileName){
        try{
            CloseFileReader();
            fileReader = new Scanner(new FileReader(FileName));
            return true;
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Used to set up File Reader with delimiter
     * @param FileName the name of the file to read from.
     * @return true if successful. False otherwise.
     */
    public boolean SetUpFileReader(String FileName, String Delimiter){
        if (SetUpFileReader(FileName)){
            fileReader.useDelimiter(Delimiter);
            return true;
        }
        return false;
    }


    /**
     * Method for writing a single line to a proper file.
     * @param Line the line that is to be writen to file.
     * @param FileName the file where the line should be written.
     * @param Append whether to append to existing data (true) or clear out and start fresh (false).
     * @return true if successful. False otherwise.
     */
    public boolean WriteToFile(String Line, String FileName, boolean Append){
        boolean task = false;
        try{
            fileWriter = new FileWriter(FileName, Append);
            fileWriter.write(Line);
            task = true;
        } catch(IOException e){
            e.printStackTrace();
            task = false;
        } finally {
            if (fileWriter != null){
                CloseFileWriter();
            }
        }

        return task;
    }


    /**
     * Method to read from file the next line.
     * @return the next line from the file name specified or null if any issue occurred.
     */
    public String ReadFromFileNextLine(){
        if (fileReader.hasNextLine()) return fileReader.nextLine();
        return null;
    }


    /**
     * Method to read from file the next int.
     * @return the next integer from the file name specified or null if any issue occurred.
     */
    public Integer ReadFromFileNextInt(){
        if (fileReader.hasNextInt()) return fileReader.nextInt();
        return null;
    }


    /**
     * Method to skip delimeter of file reader.
     */
    public void FileReaderSkipDelimeter(){
        if (fileReader != null) fileReader.skip(fileReader.delimiter());
    }


    /**
     * Method for reading the next string.
     * @return the next string read or null.
     */
    public String ReadFromFileNext(){
        if (fileReader.hasNext()) return fileReader.next();
        return null;
    }


    /**
     * Used to close File writer with proper exception handling.
     */
    private void CloseFileWriter(){
        try{
            if (fileWriter != null){
                fileWriter.close();
                fileWriter = null;
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Used to close File reader with proper exception checking.
     */
    private void CloseFileReader(){
        if (fileReader != null){
            fileReader.close();
            fileReader = null;
        }
    }
}
