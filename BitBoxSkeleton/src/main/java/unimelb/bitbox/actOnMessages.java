package unimelb.bitbox;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;
import java.nio.ByteBuffer;
import java.util.*;

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
        boolean isCreated;
        //checks if path is safe (it's not being used by anything else) and if filename exists
        if (((fileSystemManager.isSafePathName(pathname)) == true)
                && (fileSystemManager.fileNameExists(pathname) == false)) {
            //we make a fileloader to hold what we are going to write in
            try {
                //creates a file loader which the file goes into
                isCreated = fileSystemManager.createFileLoader(pathname, md5, filesize, lastmodified);
                Boolean checkedShortcut = fileSystemManager.checkShortcut(pathname);
                if (checkedShortcut) {
                    status = jsonMarshaller.Messages.fileCreated;
                } else if (isCreated) {
                    status = jsonMarshaller.Messages.ready;
                } else {
                    status = jsonMarshaller.Messages.problemCreatingFile;
                }
                //this is the message field for the json

            } catch (Exception e) {
                exceptionHandler.handleException(e);
                status = jsonMarshaller.Messages.problemCreatingFile;
            }
        } else {
            if (fileSystemManager.isSafePathName(pathname) == false) {
                status = jsonMarshaller.Messages.unsafePathname;
            } else if (fileSystemManager.fileNameExists(pathname) == true) {
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
        if (Peer.mode.equals("udp")) {
            if (blocksize > 8192) {
                blocksize = Long.valueOf(8192);
            }
        }
        if (!type.equals("FILE_CREATE_REQUEST")
                && (!type.equals("FILE_MODIFY_REQUEST"))) {
            position = unmarshalledmessage.getPosition();
            length = unmarshalledmessage.getLength();
            position = position + length;
            if ((position + length) > unmarshalledmessage.getFileSize()) {
                length = unmarshalledmessage.getFileSize() - position;
            }
            System.out.println("length: " + length + "| position: " + position);
        } else {
            position = (long) 0;
            length = Math.min(blocksize, unmarshalledmessage.getFileSize());
            System.out.println("0");
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
            System.out.println("positon" + position);
            System.out.println("length" + length);
            buffer = fileSystemManager.readFile(md5, position, length);
            System.out.println("buffer: " + buffer);
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
            Boolean done = fileSystemManager.checkWriteComplete(pathname);
            System.out.println("download status, done is: " + done + "for file: " + pathname);
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
        boolean isExist = fileSystemManager.fileNameExists(pathname);
        if (isExist == true) {
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
            statusmessage = jsonMarshaller.Messages.pathnameNotExists;
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
            } else {
                responseMessage = jsonMarshaller.Messages.problemCreatingDirectory;
            }
        }
        //makes a json with the required information
        return jsonMarshaller.createDIRECTORY_CREATE_RESPONSE(pathname, responseMessage);

    }

    public static String directoryDeleteRequestResponse(Document message) {
        //make a method that responds to a DIRECTORY_DELETE_REQUEST by actually deleting a directory
        //use dirNameExists and deleteDirectory from the FileSystemManager
        jsonunMarshaller unmarshalledmessage = new jsonunMarshaller(message);
        String pathname = unmarshalledmessage.getpathName();
        jsonMarshaller.Messages responseMessage = null;
        if ((fileSystemManager.dirNameExists(pathname) == true)
                && (fileSystemManager.isSafePathName(pathname) == true)) {
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
            } else {
                responseMessage = jsonMarshaller.Messages.problemDeletingDirectory;
            }
        }
        return jsonMarshaller.createDIRECTORY_DELETE_RESPONSE(pathname, responseMessage);
    }

    public static void generateSyncEvents() {
        ArrayList<FileSystemManager.FileSystemEvent> events = fileSystemManager.generateSyncEvents();
        for (FileSystemManager.FileSystemEvent event : events) {
            //disgusting repeated code
            String myjson = jsonMarshaller.fileEventToJson(event);
            ArrayList<clientSocket> peeroutputstreams = peerList.getPeerList();
            if (!(peeroutputstreams.isEmpty())) {
                for (clientSocket myclient : peeroutputstreams) {
                    //sends a fileSystem event that has been triggered into the output stream
                    myclient.write(myjson);
                }
            }
        }
    }

    public static String fileModifyRequestResponse(Document message) {
        System.out.println("Trying to call modify requests");
        jsonunMarshaller unmarshalledmessage = new jsonunMarshaller(message);
        String pathname = unmarshalledmessage.getpathName();
        String md5 = unmarshalledmessage.getmd5();
        Long lastmodified = unmarshalledmessage.getlastmodified();
        jsonMarshaller.Messages responseMessage = null;
        boolean isCreated;
        if ((fileSystemManager.fileNameExists(pathname, md5) == false)
                && (fileSystemManager.isSafePathName(pathname) == true)
                && (fileSystemManager.fileNameExists(pathname) == true)) {
            System.out.println("attempting to see if it's a valid path name");
            //we make a fileloader to hold what we are going to write in
            try {
                System.out.println("Atttempting to cancel file loaders");
                fileSystemManager.cancelFileLoader(pathname);
                //creates a file loader which the file goes into
                isCreated = fileSystemManager.modifyFileLoader(pathname, md5, lastmodified);
                System.out.println("try creating file loader");
                //this is the message field for the json
                if (isCreated) {
                    responseMessage = jsonMarshaller.Messages.ready;
                } else {
                    responseMessage = jsonMarshaller.Messages.problemModifyingFile;
                }
            } catch (Exception e) {
                System.out.println("an exception occurreds");
                exceptionHandler.handleException(e);
                responseMessage = jsonMarshaller.Messages.problemModifyingFile;
            }
        } else {
            if (fileSystemManager.isSafePathName(pathname) == false) {
                responseMessage = jsonMarshaller.Messages.unsafePathname;
            } else if (fileSystemManager.fileNameExists(pathname, md5) == true) {
                responseMessage = jsonMarshaller.Messages.fileExistWithSameContent;
            } else if (fileSystemManager.fileNameExists(pathname) == false) {
                responseMessage = jsonMarshaller.Messages.pathnameNotExists;
            } else {
                responseMessage = jsonMarshaller.Messages.problemModifyingFile;
            }
        }
        System.out.println("attempting to make string");
        return jsonMarshaller.createFILE_MODIFY_RESPONSE(unmarshalledmessage.getFileDescriptorDocument(), pathname,
                responseMessage);
    }

    @Override
    public void run() {
        System.out.println("acting on message");
    }

}
