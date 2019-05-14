package unimelb.bitbox;
import org.kohsuke.args4j.Option;
import unimelb.bitbox.util.HostPort;

//This is the class where the arguments from the command line will be stored
public class CmdLineArgs {

    @Option(required = true, name = "-c", usage = "Command")
    private String command;

    @Option(required = true, name = "-s", usage = "Server")
    private String server;

    @Option(required = true, name = "-i", usage = "identity")
    private String identity;

    @Option(required = false, name = "-p", usage = "Peer")
    private String peer;

    //TODO: do we need to check if the commands are valid here?
    public String getCommand(){
        return command;
    }
    public HostPort getServer(){
        return new HostPort(server);
    }
    public HostPort getPeer(){
        return new HostPort(peer);
    }
    public String getIdentity(){
        return identity;
    }

}
