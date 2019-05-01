package unimelb.bitbox;

import unimelb.bitbox.util.Document;

public class jsonunMarshaller {
    //each one of the methods in this gets a json field exactly as described in 
    //the project specifications

    Document message;
    
    jsonunMarshaller(Document message) {
        this.message = message;
    }

    public Document getFileDescriptorDocument() {
        //gets the file descriptor
        return (Document) message.get("fileDescriptor");
    }

    public String getpathName() {
        //gets the path name
        return (String) message.get("pathName");
    }

    public Long getPosition() {
        //gets the position
        return (Long) message.get("position");
    }

    public Long getLength() {
        //gets the length of the message
        return (Long) message.get("length");
    }

    public String getContent() {
        //gets the content
        return (String) message.get("content");
    }

    public String getStatus() {
        //gets the status
        return (String) message.get("status");
    }

    public String getMessage() {
        //gets the message
        return (String) message.get("message");
    }

    public String getCommand() {
        //gets the command
        return (String) message.get("command");
    }




    //within the file descriptor sub json
    public String getmd5() {
        //gets the md5
        return getFileDescriptorDocument().getString("md5");
    }

    public Long getlastmodified() {
        //gets the last modified date
        return (Long) getFileDescriptorDocument().get("lastModified");
    }

    public Long getFileSize() {
        //gets the file size
        return (Long) getFileDescriptorDocument().get("fileSize");
    }
}
