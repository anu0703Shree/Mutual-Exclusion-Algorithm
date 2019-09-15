public abstract class Message {
    
    public static final int REQUEST = 0;
    public static final int RELEASE = 1;
    public static final int ENTER = 2;
    public static final int EXIT = 3;
    public final int type;
    public final int sourceId;
    public int timeStamp;
    public int destination;
    
    public Message(int type, int sourceId, int timeStamp, int destination) {
        this.type = type;
        this.sourceId = sourceId;
        this.timeStamp = timeStamp;
        this.destination = destination;
    }
}
