import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;


public class Parser {

	private int id;
	int port;
	//change to the proper type
	Collection<NeighborInfo> neighbors;
	int numNodes;
	int IRD;
	int CET;
	int numReqs;
	
	//constructor
	//creates the parser and runs parse
	public Parser(int i) {
		id = i;
		port = 0;
		neighbors = new ArrayList<NeighborInfo>();
		numNodes = 0;
		parse();
	}
	
	
	//parses the config.txt file in the launch folder
	//gets the number of
	private void parse() {
		String line;
		Scanner parser;
		int stage = 1;
		int nodes = 0;
		int curr = 0;
		ArrayList<String> nodeLocs = new ArrayList<String>();
		String[] tokens;
		ArrayList<String> Ns = new ArrayList<String>();
		try {
			//change for later
			parser = new Scanner(new File("Proj3/config.txt"));
			
		
			while(parser.hasNextLine()) {
				line = parser.nextLine().trim();
				//checks if the line is empty or a comment
				tokens = line.split("#");
				line = tokens[0];
				tokens = line.split(" ");
				if(!line.isEmpty()) {
					if(line.charAt(0) != '#') {
						//gets the number of nodes in the system
						if(stage == 1) {
							String[] l = line.split(" ");
							nodes = Integer.parseInt(l[0]);
							IRD = Integer.parseInt(l[1]);
							CET = Integer.parseInt(l[2]);
							numReqs = Integer.parseInt(l[3]);
							numNodes = nodes;
							stage++;
						}
						//stores the list of node ids, hostnames, and port numbers
						else if(stage == 2) {
							Ns.add(line);
						}
						//finds the node and it's list of neighbors
//						else if(stage == 3) {
//							curr++;
//							tokens = line.split(" ");
//							if(Integer.parseInt(tokens[0]) == id) {
//								Ns = tokens.clone();
//							}
//						}
					}
					
				}
			}
			parser.close();
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//adds the neighbors of the nodes to neighbors
		for(String s: Ns) {
			tokens = s.split(" ");
			if(Integer.parseInt(tokens[0]) != id) {
						neighbors.add(new NeighborInfo(Integer.parseInt(tokens[0]),tokens[1],Integer.parseInt(tokens[2])));
			}
			else {
					tokens = s.split(" ");
					port = Integer.parseInt(tokens[2]);
			}
		}
	}
	
	
	//returns the neighbors
	public Collection<NeighborInfo> getNeighbors() {
		return neighbors;
	}
	
	//returns the number of nodes in the system
	public int getNumNodes() {
		return numNodes;
	}
	
	//returns the id of the process
	public int getId() {
		return id;
	}
	
	public int getPort() {
		return port;
	}
	
	public int getIRD() {
		return IRD;
	}
	
	public int getCET() {
		return CET;
	}
	
	public int getNumReqs() {
		return numReqs;
	}
	
	//prints neighbors for test purposes (assumes neighbor class has the get methods)
	
	public void printNeighbors() {
		for(NeighborInfo n: neighbors) {
			System.out.println(n.hostName +" "+ n.id +" "+ n.port);
		}
	}
	
}
