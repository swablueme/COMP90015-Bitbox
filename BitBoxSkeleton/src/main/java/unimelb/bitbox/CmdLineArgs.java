package unimelb.bitbox;
import org.kohsuke.args4j.Option;
import unimelb.bitbox.util.HostPort;

//This is the class where the arguments from the command line will be stored
public class CmdLineArgs {

    @Option(required = true, name = "-c", usage = "Command")
    private String command;

    @Option(required = true, name = "-s", usage = "Server")
    private String server;

    @Option(required = false, name = "-p", usage = "Peer")
    private String peer;

    public String getCommand(){
        return command;
    }
    public HostPort getServer(){
        return new HostPort(server);
    }
    public HostPort getPeer(){
        return new HostPort(peer);
    }

}
