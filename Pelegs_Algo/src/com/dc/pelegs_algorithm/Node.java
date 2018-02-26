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
	boolean isLeader;
	// Total acknowledgment received
	int ackCount = 0;

	boolean b = true;
	String host;
	int port;
	ServerSocket serverSocket;

	// parent, children, degree
	int parent = -1;
	ArrayList<Integer> children;
	int degree;

	int maxDegree = 0;
	int maxDegreeUID;

	boolean marked;
	int bfsRound = 0;
	CopyOnWriteArrayList<Msg> bfsBuffer;
	boolean allChildrenFound;
	boolean bsfTerminateFlag = false;
	boolean searchMsgSent = false;
	boolean ackMsgSent = false;
	int foundChildCounter=0;
	int maxDegreeSoFar = Integer.MIN_VALUE;
	int leader = -1;
	HashMap<Integer, Integer> childMaxDegree = new HashMap<>();

	public boolean isAllChildrenFound() {
		return allChildrenFound;
	}

	public void setAllChildrenFound(boolean allChildrenFound) {
		this.allChildrenFound = allChildrenFound;
	}

	public CopyOnWriteArrayList<Msg> getBfsBuffer() {
		return bfsBuffer;
	}

	public void setBfsBuffer(CopyOnWriteArrayList<Msg> bfsBuffer) {
		this.bfsBuffer = bfsBuffer;
	}

	//List of UID's of the neighbors of the node
	ArrayList<Integer> neighbors;

	//termination flag use to terminate threads listening to sockets and processing peleg's algorithm
	boolean terminatePelegsFlag=false;

	//termination flag to stop server socket on this node 
	boolean closeSocketFlag = false;

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
		this.bfsBuffer = new CopyOnWriteArrayList<>();
		this.children = new ArrayList<>();
		this.parent = -1;
		this.ackCount = 0;
		this.degree = 0;
		this.maxDegree = 0;
		this.maxDegreeUID = id;
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

	public int getParent() {
		return parent;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public ArrayList<Integer> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<Integer> children) {
		this.children = children;
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	public int getBfsRound() {
		return bfsRound;
	}

	public void setBfsRound(int bfsRound) {
		this.bfsRound = bfsRound;
	}


	public static void main(String[] args) throws IOException {

		int UID = Integer.parseInt(args[0]);

		System.out.println("Starting execution for the UID:" + UID);

		String configFile = args[1];
		int numberOfNode = 0;
		Scanner sc = new Scanner(new File(configFile));
		HashMap<Integer, Node> confMap = new HashMap<>();

		boolean neighborConfigFlag = false;
		while(sc.hasNextLine())
		{
			String line = sc.nextLine().trim();

			if(line.startsWith("# Node#   Neighbors"))
				neighborConfigFlag = true;

			if(line.startsWith("# Number of nodes"))
			{
				line = sc.nextLine().trim();
				numberOfNode = Integer.parseInt(line);
				line = sc.nextLine().trim();
			}

			if(!line.startsWith("#") && (!neighborConfigFlag) && line.length() > 0)
			{
				String[] nodeData = line.split("\\s+");

				int tempUID = Integer.parseInt(nodeData[0]);	

				Node tempNode = new Node(tempUID);
				tempNode.setHost(nodeData[1]);
				tempNode.setPort(Integer.parseInt(nodeData[2]));


				confMap.put(tempUID, tempNode);
			}

			//read neighbors
			if(!line.startsWith("#") && (neighborConfigFlag) && line.length()>0)
			{
				String[] nodeData = line.split("\\s+");
				int id = Integer.parseInt(nodeData[0]);
				Node tempNode = confMap.get(id);

				ArrayList<Integer> tempNeighbors = new ArrayList<>();

				for(int i = 1; i< nodeData.length;i++)
					tempNeighbors.add(Integer.parseInt(nodeData[i]));

				tempNode.setNeighbors(tempNeighbors);
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
