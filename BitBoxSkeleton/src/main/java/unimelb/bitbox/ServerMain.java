package unimelb.bitbox;

import java.io.BufferedWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.FileSystemObserver;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;
import java.util.concurrent.CompletableFuture;
import java.nio.ByteBuffer;
import java.util.Base64;
import unimelb.bitbox.util.Document;
import java.util.concurrent.*;

public class ServerMain implements FileSystemObserver {
    
    private static Logger log = Logger.getLogger(ServerMain.class.getName());
    protected FileSystemManager fileSystemManager;
    
    public static BlockingQueue<Document> messageQueue = new LinkedBlockingQueue<Document>();
    
    public ServerMain() throws NumberFormatException, IOException, NoSuchAlgorithmException {
        fileSystemManager = new FileSystemManager(Configuration.getConfigurationValue("path"), this);
        new actOnMessages(fileSystemManager);
    }
    
    @Override
    public void processFileSystemEvent(FileSystemEvent fileSystemEvent) {
        Logger log = Logger.getLogger(FileSystemManager.class.getName());
        //converts a fileSystemEvent into json ready to send
        String myjson = jsonMarshaller.fileEventToJson(fileSystemEvent);
        ArrayList<clientSocket> peeroutputstreams = peerList.getPeerList();
        if (!(peeroutputstreams.isEmpty())) {
            for (clientSocket myclient : peeroutputstreams) {
                //sends a fileSystem event that has been triggered into the output stream
                myclient.write(myjson);
            }
        }

        //prettyPrinter.print(myjson);
        /*
        if (fileSystemEvent.event == FileSystemManager.EVENT.FILE_CREATE) {
            log.info("processFileSystemEvent OCCURING FOR EVENT: " + fileSystemEvent.fileDescriptor.md5);
            String mymessage=jsonMarshaller.createFILE_CREATE_REQUEST(
                                            fileSystemEvent.fileDescriptor.toDoc(), 
                                            fileSystemEvent.name);
            prettyPrinter.print(mymessage);
         */
 /*THIS CREATES A FILE DUPLICATE MADE BY CREATING A BYTE64 buffer and encoding then deencoding it
 //some of the bits of code here were written up more neatly in the base64Handler
            
            try {
                ByteBuffer test = fileSystemManager.readFile(fileSystemEvent.fileDescriptor.md5, 0, 
                        fileSystemEvent.fileDescriptor.fileSize);
                System.out.println("creates an example bytebuffer");
                //reads a bytebuffer into string after getting the bytebuffer as a bytearray
                String encoded = Base64.getEncoder().encodeToString(test.array());
                //now we convert the bytebuffer back into a byte array
                byte[] barr = Base64.getDecoder().decode(encoded);
                //now we turn a bytearray into a bytebuffer
                ByteBuffer buf = ByteBuffer.wrap(barr);
                //we check if the path is alright and if the filename doesn't already exist
                if ((fileSystemManager.isSafePathName(fileSystemEvent.path+"2") == true)
                        && (fileSystemManager.fileNameExists(fileSystemEvent.pathName+"2") == false)) {
                    //we make a fileloader to hold what we are going to write in
                    fileSystemManager.createFileLoader(fileSystemEvent.name + "2", fileSystemEvent.fileDescriptor.md5, 0, 
                                    fileSystemEvent.fileDescriptor.lastModified);
                    System.out.println("MADE FILE LOADER");
                    //we write our file
                    fileSystemManager.writeFile(fileSystemEvent.name + "2", buf, 0);

                    //we check if it has been written properly
        
                    fileSystemManager.checkWriteComplete(fileSystemEvent.name + "2");
                    System.out.println("CHECKING COMPLETE");

                }

            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
         */
    }
    //System.out.println(fileSystemEvent.fileDescriptor.lastModified);
    // TODO: process events
}
