package Logic;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class B_Network {
    private static final String APPLICATION_NAME = "Project Medicinae";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";


    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = B_Network.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        System.out.println(in.toString());
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    /**
     * Method for uploading the file to google drive account.
     * @param FileName THe name of the file to upload to drive.
     * @return true if successful. False otherwise.
     */
    public static boolean UploadToDrive(String FileName){
        if (isInternetAvailable() == false){
            return false;
        }

        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Uploading files
            File fileMetadata = new File();
            fileMetadata.setName(FileName);
            java.io.File filePath = new java.io.File(FileName);
            String MIME = Files.probeContentType(filePath.toPath());
            FileContent mediaContent = new FileContent(MIME, filePath);
            File file = service.files().create(fileMetadata, mediaContent).setFields("id").execute();
            System.out.println("File ID: " + file.getId());

            return true;
        } catch (IOException e){
            System.out.println(e.getClass().getName());
        }
        catch (GeneralSecurityException e){
            System.out.println(e.getClass().getName());
        }

        return false;
    }


    /**
     * Method for getting the file list in google drive.
     * @return true if successful. False otherwise.
     */
    public static boolean DownloadFromDrive(String FileName){
        if (isInternetAvailable() == false){
            return false;
        }

        try{
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME).build();

            // Print the names and IDs for up to 10 files.
            while(true) {
                FileList result = service.files().list().setPageSize(25).setFields("nextPageToken, files(id, name)").execute();
                List<File> files = result.getFiles();
                if (files == null || files.isEmpty()) {
                    System.out.println("No files found.");
                    break;
                } else {
                    System.out.println("Files:");
                    for (File file : files) {
                        System.out.printf("%s (%s)\n", file.getName(), file.getId());
                        if (file.getName().equals(FileName)) {
                            OutputStream outputStream = new ByteArrayOutputStream();
                            service.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
                            B_FileSystem.B_FileSystem_instance.WriteToFile(outputStream.toString(), FileName, false);
                            outputStream.flush();   outputStream.close();
                            return true;
                        }
                    }
                }
            }
        }
        catch (IOException e){
            System.out.println(e.getClass().getName());
        }
        catch (GeneralSecurityException e){
            System.out.println(e.getClass().getName());
        }

        return true;
    }


    /**
     * Method for checking whether an internet connection is available.
     * @return true if available, otherwise false.
     * @throws IOException
     */
    private static boolean isInternetAvailable() {
        try {
            return isHostAvailable("google.com") || isHostAvailable("amazon.com")
                    || isHostAvailable("facebook.com") || isHostAvailable("apple.com");
        }
        catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isHostAvailable(String hostName) throws IOException {
        try(Socket socket = new Socket()) {
            int port = 80;
            InetSocketAddress socketAddress = new InetSocketAddress(hostName, port);
            socket.connect(socketAddress, 3000);

            return true;
        }
        catch(UnknownHostException unknownHost) {
            return false;
        }
    }
}