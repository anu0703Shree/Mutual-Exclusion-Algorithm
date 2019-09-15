import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;

public class Node {
    
    
    public final int id;
    private int currHopCount = 1;
    private boolean startedNetworkDiscovery;
    private int seqNumber = 1;
    private ServerSocket serverSocket;
    private Map<Integer, ReachInfo> knownNodes = new HashMap<>();
    private Map<Integer, NeighborHandler> neighbors = new ConcurrentHashMap<>();
    private List<Integer> neighborIDs = new ArrayList<>();
    private Map<Integer, NeighborInfo> neighborsInfo = 
            new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ExecutorService executor2 = Executors.newSingleThreadExecutor();
    private ExecutorService executor3 = Executors.newCachedThreadPool();
    private ExecutorService executor4 = Executors.newCachedThreadPool();
    public ScheduledExecutorService scheduledExecutor = 
            Executors.newSingleThreadScheduledExecutor();
    public Map<Integer, Boolean> permissions = new ConcurrentHashMap<>();
    public Map<Integer, Boolean> pendingRequests = new ConcurrentHashMap<>();
    public Map<Integer, Integer> timeStamps = new ConcurrentHashMap<>();
    public boolean waitingForCS = false;
    public boolean inCS = false;
    public int timeStamp = 0;
    public int numMessages;
    public RequestMessage currentRequest;
    public int IRD;
	public int CET;
	public Semaphore TSSema = new Semaphore(1);
	public int MessagesSent = 0;
    
    public Node(int id, String hostName, int port){
        this.id = id;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(hostName, port));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public void peer(final Collection<NeighborInfo> peers) {
        for (NeighborInfo peer : peers) {
            if (peer.id < id) {
                connect(peer);
            }
        }
    }
    
    public boolean connectionSetupDone() {
        return neighborIDs.size() == neighbors.size();
    }
    
    private void setNeighbors(final Collection<NeighborInfo> peers) {
    	acquire();
        for (NeighborInfo peer : peers) {
            neighborsInfo.put(peer.id, peer);
            neighborIDs.add(peer.id);
            timeStamps.put(peer.id,Integer.MAX_VALUE);
            if(peer.id > id) {
            	permissions.put(peer.id, true);
            }
            else {
            	permissions.put(peer.id, false);
            }
            pendingRequests.put(peer.id, false);
        }
        release();
    }
    
    public void start() {
        Parser parser = new Parser(id);
        IRD = parser.getIRD();
        CET = parser.getCET();
        numMessages = parser.getNumReqs();
        setNeighbors(parser.getNeighbors());
        peer(parser.getNeighbors());
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Socket socket = serverSocket.accept();
                        handleClient(socket);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        sleep(1000);
                    }
                }
            }
        }).start();
    }
    
    private void handleClient(final Socket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NeighborInfo peer = getNeighborInfo(socket);
                if (peer != null) {
                    createChannel(peer, socket);
                }
            }
        }).start();
    }
    
    private NeighborInfo getNeighborInfo(final Socket client) {       
        try {
            DataInputStream dis = new DataInputStream(client.getInputStream());
            int peerId = dis.readInt();
            if (neighborsInfo.containsKey(peerId)) {
                return neighborsInfo.get(peerId);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }
    
    private void createChannel(final NeighborInfo peer, final Socket socket) {
        try {
            DataInputStream dis = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(socket.getOutputStream()));
            NeighborHandler neighborHandler = new NeighborHandler(
                    peer, socket, dis, dos);
            neighbors.put(peer.id, neighborHandler);
            neighborHandler.start();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            sleep(1000);
        }
        if (connectionSetupDone()) {
            System.out.println("Node " + id + ": connection setup done!");
        }
    }
    
    
    public void connect(final NeighborInfo peer) {
        boolean connected = false;
        while (!connected) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(peer.hostName, peer.port));
                DataOutputStream dos = new DataOutputStream(
                        socket.getOutputStream());
                dos.writeInt(id);
                dos.flush();
                createChannel(peer, socket);
                connected = true;
            } catch (ConnectException ce) {
                System.out.println("Failed to connect to " + peer.hostName 
                        + " retrying...");
                sleep(1000);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } 
        }
    }
    
    public void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
    
    public void sendRequestMessage(RequestMessage msg) {
    	if (neighbors.containsKey(msg.destination)){
    		neighbors.get(msg.destination).sendRequestMessage(msg);
    	}
    }
    
    public void sendReleaseMessage(ReleaseMessage msg) {
    	if (neighbors.containsKey(msg.destination)){
    		neighbors.get(msg.destination).sendReleaseMessage(msg);
    	}
    }

    public boolean CSEnter() throws InterruptedException {
    	acquire();
    	waitingForCS = true;
    	timeStamp++;
    	currentRequest = new RequestMessage(Message.REQUEST,id,timeStamp,id);
    	SendRequestMessageTask srmt = new SendRequestMessageTask();
    	release();
    	srmt.run();
    	while(true) {
    		acquire();
    		if(!permissions.containsValue(false)) {
    			inCS = true;
    			release();
    			return true;
    		}
    		else {
//    			System.out.println("Waiting on CS");
//    			System.out.println(permissions);
    			release();
//    			sleep(1000);
    		}
    	}
    }
    
    public boolean CSLeave() {
    	acquire();
    	waitingForCS = false;
    	inCS = false;
    	currentRequest = new RequestMessage(Message.REQUEST,id,Integer.MAX_VALUE,id);
    	release();
    	sendPendingRequests();
    	return true;
    }
    
    private void sendPendingRequests() {
		acquire();
    	for(Map.Entry<Integer, Boolean> n: pendingRequests.entrySet()) {
			if(n.getValue()) {
				neighbors.get(n.getKey()).sendReleaseMessage(new ReleaseMessage(Message.RELEASE, id, timeStamp, n.getKey()));
				pendingRequests.put(n.getKey(), false);
				permissions.put(n.getKey(), false);
			}
		}
		release();
	}

	private class NeighborHandler extends Thread {
    
        final NeighborInfo neighborInfo;
        final Socket socket;
        final DataInputStream dis;
        final DataOutputStream dos;

        public NeighborHandler(final NeighborInfo neighborInfo, 
                               final Socket socket, 
                               final DataInputStream dis, 
                               final DataOutputStream dos) {
            this.neighborInfo = neighborInfo;
            this.socket = socket;
            this.dis = dis;
            this.dos = dos;
        }


		@Override
        public void run() {
            while(true) {
                try {
                    int messageType = dis.readInt();
                    switch(messageType) {
                        case Message.REQUEST: 
                            RequestMessage reqmsg = readRequestMessage();
                            executor3.execute(
                                    new RequestMessageReceivedTask(reqmsg));
                            break;
                        case Message.RELEASE:
                            ReleaseMessage relmsg = readReleaseMessage();
                            executor4.execute(
                                    new ReleaseMessageReceivedTask(relmsg));
                            break;
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        
        
        public RequestMessage readRequestMessage() throws 
		        IOException {
		    int sourceId = dis.readInt();
		    int timeStamp = dis.readInt();
		    int destination = dis.readInt();
		    RequestMessage msg = new RequestMessage(Message.REQUEST, sourceId, timeStamp, destination);
//		    System.out.println("Reading: "+msg);
		    return msg;
        }
        
        public void sendRequestMessage(RequestMessage msg) {
	    	MessagesSent++;
            try {
//            	System.out.println("Sending: "+msg);
                dos.writeInt(msg.type);
                dos.writeInt(msg.sourceId);
                dos.writeInt(msg.timeStamp);
                dos.writeInt(msg.destination);
                dos.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        
        public ReleaseMessage readReleaseMessage() throws 
		        IOException {
		    int sourceId = dis.readInt();
		    int timeStamp = dis.readInt();
		    int destination = dis.readInt();
		    ReleaseMessage msg = new ReleaseMessage(Message.RELEASE, sourceId, timeStamp, destination);
//		    System.out.println("Reading: "+msg);
		    return msg;
	    }
    
	    public void sendReleaseMessage(ReleaseMessage msg) {
//	    	System.out.println("Sending: "+msg);
	    	MessagesSent++;
	        try {
	            dos.writeInt(msg.type);
	            dos.writeInt(msg.sourceId);
	            dos.writeInt(msg.timeStamp);
	            dos.writeInt(msg.destination);
	            dos.flush();
	        } catch (IOException ioe) {
	            ioe.printStackTrace();
	        }
	    }
    }
        

    
    private class RequestMessageReceivedTask implements Runnable {
    
        private final RequestMessage msg;
        
        public RequestMessageReceivedTask(final RequestMessage msg) {
            this.msg = msg;
        }
        
        @Override
        public void run() {
			acquire();
        	if(msg.timeStamp > timeStamp) {
        		timeStamp = msg.timeStamp;
        	}
        	timeStamps.put(msg.sourceId, msg.timeStamp);
        	
        	if (inCS || (msg.timeStamp > currentRequest.timeStamp || (msg.timeStamp == currentRequest.timeStamp && msg.sourceId > id))) {
        		addRequestToPending(msg.sourceId);
        	}else{
        		permissions.put(msg.sourceId,false);
        		neighbors.get(msg.sourceId).sendReleaseMessage(new ReleaseMessage(Message.RELEASE, id, timeStamp, msg.sourceId));
        		if(waitingForCS) {
            		neighbors.get(msg.sourceId).sendRequestMessage(new RequestMessage(Message.REQUEST,id,currentRequest.timeStamp,msg.sourceId));
        		}
        	}
        	release();
        }

		private void addRequestToPending(int sourceId) {
			pendingRequests.put(sourceId, true);
			
		}
        
        
    }
    
    private class ReleaseMessageReceivedTask implements Runnable {
        
        private final ReleaseMessage msg;
        
        public ReleaseMessageReceivedTask(final ReleaseMessage msg) {
            this.msg = msg;
//            System.out.println("Received"+ msg.toString());
        }
        
        @Override
        public void run() { 
        	acquire();
        	permissions.put(msg.sourceId, true);
        	timeStamps.put(msg.sourceId, Integer.MAX_VALUE);
        	release();
        	
            //change relevant permission to true
        	//check if all permissions are true, and waiting for critical section
        		//if so, set inCS to true and allow main to enter critical section
        }
        
        
    }
    
    private class SendRequestMessageTask implements Runnable {

        ArrayList<RequestMessage> messages = new ArrayList<>();
        
        public SendRequestMessageTask() {
            for (Integer i : neighborIDs ) {
                if (!permissions.get(i)) {
                    messages.add(createRequestMessage(i));
                }
            }
        }
        
        @Override
        public void run() {
            for (RequestMessage msg : messages) {
            	sendRequestMessage(msg);
            }
        } 
        
        private RequestMessage createRequestMessage(final int destination) {
            return new RequestMessage(Message.REQUEST, id, currentRequest.timeStamp, destination);
        }
    }
    
    private void acquire() {
    	try {
			TSSema.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void release() {
    	TSSema.release();
    }
}
