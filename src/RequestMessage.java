
public class RequestMessage extends Message{
	
	
	public RequestMessage(int type, int sourceId, int timeStamp, int destination) {
		super(type, sourceId, timeStamp, destination);
		
	}
	public String toString() {
		return "Type: Request	Source: "+sourceId + "\tTime Stamp: " + timeStamp + "\tDestination: "+ destination;
	}
}
