package unimelb.bitbox;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;
import java.nio.ByteBuffer;

public class actOnMessages implements Runnable {

    static FileSystemManager fileSystemManager;

    actOnMessages(FileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
    }

   
    //a response to a file create
    public static String fileCreateResponse(Document message) {
        //creates a jsonunmarshaller object which has convenient methods for getting information out of a Document 
        //(structure that contains json data)
        jsonunMarshaller unmarshalledmessage = new jsonunMarshaller(message);
        String pathname = unmarshalledmessage.getpathName();
        String md5 = unmarshalledmessage.getmd5();
        Long lastmodified = unmarshalledmessage.getlastmodified();
        Long filesize = unmarshalledmessage.getFileSize();
        //initialise messages variable
        jsonMarshaller.Messages status = null;
        //checks if path is safe (it's not being used by anything else) and if filename exists
        if (((fileSystemManager.isSafePathName(pathname)) == true)
                && (fileSystemManager.fileNameExists(pathname) == false)) {
            //we make a fileloader to hold what we are going to write in     
            try {
                //creates a file loader which the file goes into
                fileSystemManager.createFileLoader(pathname, md5, filesize, lastmodified);
                Boolean checkedShortcut = fileSystemManager.checkShortcut(pathname);
                if (checkedShortcut) {
                    status = jsonMarshaller.Messages.fileCreated;
                }
                //this is the message field for the json
                status = jsonMarshaller.Messages.ready;
            } catch (Exception e) {
                exceptionHandler.handleException(e);
                status = jsonMarshaller.Messages.problemCreatingFile;
            }
        } else {
            if(fileSystemManager.isSafePathName(pathname) == false){
                status = jsonMarshaller.Messages.unsafePathname;
            }
            else if(fileSystemManager.fileNameExists(pathname) == true){
                status = jsonMarshaller.Messages.pathnameExists;
            }
        }
        //makes a json with the required information
        return jsonMarshaller.createFILE_CREATE_RESPONSE(unmarshalledmessage.getFileDescriptorDocument(), pathname, status);
    }

    public static String fileBytesRequest(String type, Document message) {
        jsonunMarshaller unmarshalledmessage = new jsonunMarshaller(message);
        String pathname = unmarshalledmessage.getpathName();
        Long length = null;
        Long position = null;
        Long blocksize = Long.parseLong(Configuration.getConfiguration().get("blockSize"));
        if (!type.equals("FILE_CREATE_REQUEST")) {
            position = unmarshalledmessage.getPosition();
            length = unmarshalledmessage.getLength();
            position = position + length;
            if ((position+length) > unmarshalledmessage.getFileSize()) {
                length=unmarshalledmessage.getFileSize()-position;
            }
            System.out.println("length: "+ length+"| position: "+position);
        } else {
            position = (long) 0;
            length = Math.min(blocksize, unmarshalledmessage.getFileSize());
        }
        return jsonMarshaller.createFILE_BYTES_REQUEST(unmarshalledmessage.getFileDescriptorDocument(), pathname, position, length);
    }

    public static String fileBytesRequestResponse(Document message) {
        System.out.println("inside the request response");
        jsonunMarshaller unmarshalledmessage = new jsonunMarshaller(message);
        String pathname = unmarshalledmessage.getpathName();
        Long position = unmarshalledmessage.getPosition();
        Long length = unmarshalledmessage.getLength();

        String md5 = unmarshalledmessage.getmd5();
        Long lastmodified = unmarshalledmessage.getlastmodified();
        ByteBuffer buffer = null;
        jsonMarshaller.Messages status = null;
        String base64 = null;
        System.out.println("attempting to try and read");
        try {
            System.out.println("trying to read");
            System.out.println("positon"+position);
            System.out.println("length"+length);
            buffer = fileSystemManager.readFile(md5, position, length);
            System.out.println("buffer: "+buffer);
            status = jsonMarshaller.Messages.successfulRead;
        } catch (Exception e) {
            System.out.println("Exception occurred");
            exceptionHandler.handleException(e);
            status = jsonMarshaller.Messages.unsuccessfulRead;
        }
        return jsonMarshaller.createFILE_BYTES_RESPONSE(unmarshalledmessage.getFileDescriptorDocument(),
                pathname, position, length, status, buffer);
    }

    public static String processReceivedFile(Document message) {
        //writes a file that has been sent to disk
        jsonunMarshaller unmarshalledmessage = new jsonunMarshaller(message);
        String pathname = unmarshalledmessage.getpathName();
        String content = unmarshalledmessage.getContent();
        ByteBuffer contentbuffer = base64Handler.base64toByteBuffer(content);
        Long position = unmarshalledmessage.getPosition();
        try {
            fileSystemManager.writeFile(pathname, contentbuffer, position);
            //System.out.println("writing position: "+position);
            Boolean done =fileSystemManager.checkWriteComplete(pathname);
            System.out.println("download status, done is: "+done+"for file: "+pathname);
            if (!done) {
                String request = fileBytesRequest("more bytes", message);
                return request;
            } else {
                System.out.println("writing done!");
                return "done";
            }
        } catch (Exception e) {
            System.out.println("exception");
            exceptionHandler.handleException(e);
        }
        return "issue";
    }

    public static String fileDeleteResponse(Document message) {
        //deletes a file
        jsonunMarshaller unmarshalledmessage = new jsonunMarshaller(message);
        String pathname = unmarshalledmessage.getpathName();
        String md5 = unmarshalledmessage.getmd5();
        Long lastmodified = unmarshalledmessage.getlastmodified();
        jsonMarshaller.Messages statusmessage = null;
        //checks if a file exists and tries to delete it
        if (fileSystemManager.fileNameExists(pathname) == true) {
            try {
                Boolean status = fileSystemManager.deleteFile(pathname, lastmodified, md5);
                if (status == true) {
                    //sets status messages if it deleted
                    statusmessage = jsonMarshaller.Messages.fileDeleted;
                } else {
                    //otherwise it didn't get deleted
                    statusmessage = jsonMarshaller.Messages.problemDeletingFile;
                }
            } catch (Exception e) {
                exceptionHandler.handleException(e);
                statusmessage = jsonMarshaller.Messages.problemDeletingFile;
            }
        } else {
        }
        return jsonMarshaller.createFILE_DELETE_RESPONSE(unmarshalledmessage.getFileDescriptorDocument(), pathname, statusmessage);
    }

    public static String directoryCreateRequestResponse(Document message) {
        //make a method that responds to a DIRECTORY_CREATE_REQUEST by actually creating a directory
        //use dirNameExists and makeDirectory from the FileSystemManager
        jsonunMarshaller unmarshalledmessage = new jsonunMarshaller(message);
        String pathname = unmarshalledmessage.getpathName();
        jsonMarshaller.Messages responseMessage = null;
        if (((fileSystemManager.isSafePathName(pathname)) == true)
                && (fileSystemManager.dirNameExists(pathname) == false)) {
            try {
                //creates the directory
                fileSystemManager.makeDirectory(pathname);
                //this is the message field for the json
                responseMessage = jsonMarshaller.Messages.directoryCreated;
            } catch (Exception e) {
                exceptionHandler.handleException(e);
                responseMessage = jsonMarshaller.Messages.problemCreatingDirectory;
            }
        } else {
            if (fileSystemManager.isSafePathName(pathname) == false) {
                responseMessage = jsonMarshaller.Messages.unsafePathname;
            } else if (fileSystemManager.dirNameExists(pathname) == true) {
                responseMessage = jsonMarshaller.Messages.pathnameExists;
            }
        }
        //makes a json with the required information
        return jsonMarshaller.createDIRECTORY_CREATE_RESPONSE(pathname,responseMessage);

    }

    public static String directoryDeleteRequestResponse(Document message) {
        //make a method that responds to a DIRECTORY_DELETE_REQUEST by actually deleting a directory
        //use dirNameExists and deleteDirectory from the FileSystemManager
        jsonunMarshaller unmarshalledmessage = new jsonunMarshaller(message);
        String pathname = unmarshalledmessage.getpathName();
        jsonMarshaller.Messages responseMessage = null;
        if ((fileSystemManager.dirNameExists(pathname) == true)
                && (fileSystemManager.isSafePathName(pathname) == true)){
            try {
                Boolean status = fileSystemManager.deleteDirectory(pathname);
                if (status == true) {
                    //sets status messages if it deleted
                    responseMessage = jsonMarshaller.Messages.directoryDeleted;
                } else {
                    //otherwise it didn't get deleted
                    responseMessage = jsonMarshaller.Messages.problemDeletingDirectory;
                }
            } catch (Exception e) {
                exceptionHandler.handleException(e);
                responseMessage = jsonMarshaller.Messages.problemDeletingDirectory;
            }
        } else {
            if (fileSystemManager.isSafePathName(pathname) == false) {
                responseMessage = jsonMarshaller.Messages.unsafePathname;
            } else if (fileSystemManager.dirNameExists(pathname) == false) {
                responseMessage = jsonMarshaller.Messages.pathnameNotExists;
            }
        }
            return jsonMarshaller.createDIRECTORY_DELETE_RESPONSE(pathname,responseMessage);
    }
    
    public static void generateSyncEvents() {
        fileSystemManager.generateSyncEvents();
    }
    @Override
    public void run() {
        System.out.println("acting on message");
    }
           

}
