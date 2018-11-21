# Project Medicinae
A application to keep track of patient information. Developed using Java.

## Description
An application for inputting, storing and backing up data made for doctors. The application was developed to ensure that valuable data isn't lost due to machine failure or viruses.
### Key Features
**1. Fast Data Storage:** The application uses Oracle's Database to locally store data.  
**2. Local Backup:** The application has a local back up of the data inputted into the database. Data is saved in files so that they can be retrieved in case of Oracle's Database fails for any reason.  
**3. Cloud Backup:** The application uses Google Drive to back up information. This ensures that in case of machine failure the data is never lost.
### Built With
**1. Programming language:** [Java 11 SDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)  
**2. Frontend:** [JavaFX SDK](https://www.oracle.com/technetwork/java/javafx/install-javafx-sdk-1-2-139156.html), CSS  
**3. Backend:** [Oracle Database / Oracle SQL](https://www.oracle.com/technetwork/database/express-edition/overview/index-100989.html)  
**4. API:** [Google Client API](https://developers.google.com/api-client-library/java/), [Google Drive API](https://developers.google.com/api-client-library/java/apis/drive/v3), [OJDBC](https://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html), [Servlet](http://www.java2s.com/Code/Jar/j/Downloadjavaxservletjar.htm)  
**4. IDE:** [IntelliJ IDEA](https://www.jetbrains.com/idea/)
##Prerequisites
The following software are necessary and needs to be installed to use this application.

```
Java Run Time Environment, Oracle Database 11G XE Edition 
```  
The following permissions need to be granted in order to use the application.
```
Folder Read and edit, Internet access, Google API Services and Google Drive access for a google account
```
## Installing
Follow the steps mentioned below to setup necessary development environment.   
**Step 1:** Install Java 11 SDK  
**Step 2:** Install JavaFX SDK  
**Step 3:** Install Oracle SQL 11G XE  
**Step 4:** Install IntelliJ Idea IDE.  
**Step 5:** Download necessary google API and add to library.  
**Step 6:** Prepare project by adding libraries and modules as necessary.   
**Step 7:** Create SQL user named "ishrak" and password "dragonsword05".
**Step 8:** Grant user proper permissions.  
Ready to start developing.
## Author
* [**Shakleen Ishfar**](https://github.com/Shakleen)
## License
Open source software.
## Acknowledgement  
* **My parents for their relentless support**  
* **Stack OverFlow community**  
* **Google Developers Guide**
* **Tim Buchalka and  Tim Buchalka's Learn Programming Academy** 