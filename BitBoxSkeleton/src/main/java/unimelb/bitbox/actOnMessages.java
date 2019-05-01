package unimelb.bitbox;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;
import java.nio.ByteBuffer;

public class actOnMessages {

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
        //initialise messages variable
        jsonMarshaller.Messages status = null;
        //checks if path is safe (it's not being used by anything else) and if filename exists
        if (((fileSystemManager.isSafePathName(pathname)) == true)
                && (fileSystemManager.fileNameExists(pathname) == false)) {
            //we make a fileloader to hold what we are going to write in     
            try {
                //creates a file loader which the file goes into
                fileSystemManager.createFileLoader(pathname, md5, 0, lastmodified);
                //this is the message field for the json
                status = jsonMarshaller.Messages.ready;
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
        } else {
            //otherwise we couldn't create the file
            status = jsonMarshaller.Messages.unsafePathname;
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
            position = Math.min(unmarshalledmessage.getFileSize(), position + blocksize);
        } else {
            position = (long) 0;
            //need to change later
            length = unmarshalledmessage.getFileSize();
        }
        return jsonMarshaller.createFILE_BYTES_REQUEST(unmarshalledmessage.getFileDescriptorDocument(), pathname, position, length);
    }

    public static String fileBytesRequestResponse(Document message) {
        jsonunMarshaller unmarshalledmessage = new jsonunMarshaller(message);
        String pathname = unmarshalledmessage.getpathName();
        Long position = unmarshalledmessage.getPosition();
        Long length = unmarshalledmessage.getLength();

        String md5 = unmarshalledmessage.getmd5();
        Long lastmodified = unmarshalledmessage.getlastmodified();
        ByteBuffer buffer = null;
        jsonMarshaller.Messages status = null;
        String base64 = null;
        try {
            buffer = fileSystemManager.readFile(md5, position, length);
            status = jsonMarshaller.Messages.successfulRead;
        } catch (Exception e) {
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
            if (!fileSystemManager.checkWriteComplete(pathname)) {
                String request = fileBytesRequest("more bytes", message);
                return request;
            } else {
                return "done";
            }
        } catch (Exception e) {
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
        //TODO make a method that responds to a DIRECTORY_CREATE_REQUEST by actually creating a directory
        //use dirNameExists and makeDirectory from the FileSystemManager
        return "";
    }

    public static String directoryDeleteRequestResponse(Document message) {
        //TODO make a method that responds to a DIRECTORY_DELETE_REQUEST by actually deleting a directory
        //use dirNameExists and deleteDirectory from the FileSystemManager
        return "";
    }

}
