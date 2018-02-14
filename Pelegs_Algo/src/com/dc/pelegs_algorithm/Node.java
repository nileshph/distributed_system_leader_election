package com.dc.pelegs_algorithm;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * A class to store node specific data
 */
public class Node {

	int UID;
	int x;
	int d = 0;
	int c = 0;
	int round=0;

	boolean b = true;
	String host;
	int port;
	ServerSocket serverSocket;

	// parent, children, degree
	
	//List of UID's of the neighbors of the node
	ArrayList<Integer> neighbors;

	//termination flag use to terminate threads listening to sockets and processing peleg's algorithm
	boolean termination=false;

	//map of configuration file with UID as a key
	static HashMap<Integer, Node> configMap;

	/*
	 * Thread safe buffer of messages received till now
	 */
	CopyOnWriteArrayList<Msg> msgBuffer;

	public static HashMap<Integer, Node> getConfigMap() {
		return configMap;
	}

	public static void setConfigMap(HashMap<Integer, Node> configMap) {
		Node.configMap = configMap;
	}

	Node(int id)
	{
		this.UID = id;
		this.x = id;
		//thread safe buffer for messages
		this.msgBuffer = new CopyOnWriteArrayList<>();
	}

	public List<Msg> getMsgBuffer() {
		return msgBuffer;
	}

	public void setMsgBuffer(CopyOnWriteArrayList<Msg> msgBuffer) {
		this.msgBuffer = msgBuffer;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public ArrayList<Integer> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(ArrayList<Integer> neighbors) {
		this.neighbors = neighbors;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}


	public static void main(String[] args) throws IOException {

		int UID = Integer.parseInt(args[0]);

		System.out.println("Starting execution for the UID:" + UID);
		
		String configFile = args[1];
		int numberOfNode = 0;
		Scanner sc = new Scanner(new File(configFile));
		HashMap<Integer, Node> confMap = new HashMap<>();

		while(sc.hasNextLine())
		{
			String line = sc.nextLine();

			if(line.startsWith("# Number of nodes"))
			{
				line = sc.nextLine();
				numberOfNode = Integer.parseInt(line);
				line = sc.nextLine();
			}

			if(!line.startsWith("#"))
			{
				String[] nodeData = line.split(" ");

				int tempUID = Integer.parseInt(nodeData[0]);	

				Node tempNode = new Node(tempUID);
				tempNode.setHost(nodeData[1]);
				tempNode.setPort(Integer.parseInt(nodeData[2]));

				ArrayList<Integer> tempNeighbors = new ArrayList<>();

				for(int i = 3; i< nodeData.length;i++)
					tempNeighbors.add(Integer.parseInt(nodeData[i]));

				tempNode.setNeighbors(tempNeighbors);
				confMap.put(tempUID, tempNode);
			}
		}
		
		Node.setConfigMap(confMap);
		Node thisNode = Node.getConfigMap().get(UID);

		ServerSocket socket = new ServerSocket(thisNode.getPort(), numberOfNode);

		thisNode.setServerSocket(socket);

		System.out.println("Config file read, Socket created, halting for 10 sec");
		
		/*
		 * Start client manager process which will keep accepting incoming connections to socket
		 * read messages and store them into thread safe buffer
		 */
		ClientManager ct = new ClientManager(thisNode);
		Thread t = new Thread(ct);
		t.start();

		/*
		 * halt processing for 10 seconds to make sure other nodes are up and accepting socket connections
		 */
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Client manager started");
		
		/*
		 * Create another thread to process Peleg's leader election algorithm
		 */
		PelegsProcessor pp = new PelegsProcessor(thisNode);
		
		Thread processorThread = new Thread(pp);
		
		processorThread.start();
		
		System.out.println("Pelegs processor thread started");

		sc.close();
	}

}
