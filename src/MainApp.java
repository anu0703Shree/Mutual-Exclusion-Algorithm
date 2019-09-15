import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainApp {

    public static void main(String[] args) throws InterruptedException, IOException {
    	
    	Node n1 = new Node(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
        n1.start();
        
        String ResponseTimes = "";
        long resStart;
        long resEnd;
        long ResponseTimeAvg = 0;
        long StartTime;
        long EndTime;
        int Messages;
        
        while(!n1.connectionSetupDone()) {
        	n1.sleep(1);
        }
        System.out.println("Ready to make Critical Section Requests");
        File test = new File("CS");
        
        StartTime = System.currentTimeMillis();
        for(int x = 0; x < n1.numMessages; x++) {
        	resStart = System.currentTimeMillis();
		    n1.CSEnter();
		    System.out.println("Entering Critical Section: " + x);
		    criticalSectionCheck(test);
		    n1.sleep(getExponentialRandomVariable(n1.CET));
		    System.out.println("Leaving Critical Section: " + x);
		    test.delete();
		    resEnd = System.currentTimeMillis();
		    ResponseTimeAvg += resEnd-resStart;
		    n1.CSLeave();
		    n1.sleep(getExponentialRandomVariable(n1.IRD));
        }
        EndTime = System.currentTimeMillis();
        System.out.println("Finished Critical Sections");
        ResponseTimeAvg = ResponseTimeAvg/n1.numMessages;
        double Throughput = ((long)(n1.numMessages)/((EndTime-StartTime)/1000.0));
        System.out.println(EndTime-StartTime);
        System.out.println(ResponseTimeAvg);
        System.out.println(n1.MessagesSent);
        FileWriter f = new FileWriter(new File("log.txt"),true);
        String output = Throughput + "," +  ResponseTimeAvg + "," + n1.MessagesSent + "\n";
        f.append(output);
        f.close();
    }   
    
    public static int getExponentialRandomVariable(int mean) {
    	return (int) ((-1)*(mean)*Math.log(1-Math.random())/Math.log(2));
    }
    
    private static void criticalSectionCheck(File f) throws IOException, InterruptedException{
    	if(f.exists()) {
	    	System.out.println("CRITICAL SECTION VIOLATED");
	    	Thread.sleep(3000);
	    }
	    else {
	    	f.createNewFile();
	    }
    }
    
}
