public class NeighborInfo {
    
    public final int id;
    public final String hostName;
    public final int port;
    
    public NeighborInfo(final int id, final String hostName, final int port) {
        this.id = id;
        this.hostName = hostName;
        this.port = port;
    }

    @Override
    public String toString() {
        return id + " " + hostName + " " + port;
    }
}
