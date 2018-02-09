package com.dc.pelegs_algorithm;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
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

	//List of UID's of the neighbors of the node
	ArrayList<Integer> neighbors;

	//termination flag use to terminate threads listening to sockets and processing peleg's algorithm
	boolean termination=false;

	//map of configuration file with UID as a key
	static HashMap<Integer, Node> configMap;

	//Buffer of messages to keep system synchronized
	//make sure to move to particular round only if messages for that round are received from all the
	//neighbors

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

		System.out.println("Config file read, Socket created");
		
		//start the client manager thread which will accept msg from clients and
		//store them into buffer
		ClientManager ct = new ClientManager(thisNode);
		Thread t = new Thread(ct);
		t.start();

		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Client manager started");
		
		//create one more thread to do processing of the peleg's algorithm
		PelegsProcessor pp = new PelegsProcessor(thisNode);
		
		Thread processorThread = new Thread(pp);
		
		processorThread.start();
		
		System.out.println("Pelegs processor started");

		sc.close();
	}

}
