
public class ReleaseMessage extends Message {

	public ReleaseMessage(int type, int sourceId, int timeStamp, int destination) {
		super(type, sourceId, timeStamp, destination);
		
	}
	
	public String toString() {
		return "Type: Release	Source: "+sourceId + "\tTime Stamp: " + timeStamp + "\tDestination: "+ destination;
	}
	
}
