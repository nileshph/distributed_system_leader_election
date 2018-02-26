package com.dc.pelegs_algorithm;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BFS1 implements Runnable {

	Node currNode;

	public BFS1(Node thisNode) {
		this.currNode = thisNode;
	}

	@Override
	public void run() {

		while(!currNode.bsfTerminateFlag)
		{
			if(currNode.isLeader && !currNode.searchMsgSent)
			{
				currNode.searchMsgSent = true;
				currNode.parent = -1;
				currNode.setMarked(true);
				sendSearchMsgtoAll();
			}
			else
			{
				processMessages();
			}
		}
		// BFS completed, time to close client manager thread
		currNode.closeSocketFlag = true;
	}

	private void processMessages() {
		List<Msg> bfsBuffer = currNode.getBfsBuffer();
		synchronized (bfsBuffer) {
			for (Iterator<Msg> iterator = bfsBuffer.iterator(); iterator.hasNext();) {
				Msg msg = iterator.next();

				if(msg.bfsMsgFlag)
				{
					// search message arrived at already marked node
					if(!msg.messageTypeAck && currNode.isMarked())
						sendNegAck(msg);
					else
						// search message arrived at unmarked node
						if(!msg.messageTypeAck && !currNode.isMarked())
						{
							currNode.marked=true;
							currNode.parent = msg.senderUID;
							currNode.searchMsgSent = true;
							sendSearchMsgtoAll();
						}
						else
							// positive/negative acknowledgement arrived
							if(msg.messageTypeAck)
							{
								currNode.foundChildCounter++;
								if(msg.ack && msg.senderUID != currNode.UID) {
									//System.out.println(msg.senderUID + " added as child on " + currNode.UID);
									currNode.children.add(msg.senderUID);
									if(currNode.maxDegree < msg.maxDegree)
										currNode.maxDegree = msg.maxDegree;
								}

								if(currNode.foundChildCounter == currNode.neighbors.size())
								{
									// All Acknowledgement found, time to converge cast
									int parentId = currNode.getParent();

									currNode.bsfTerminateFlag = true;
									System.out.println("BFS terminated at " + currNode.UID);
									System.out.println("BFS Parent is " + currNode.parent);
									int degree =0;
									if(parentId!=-1)
									{
										degree++;
									}
									if(currNode.children!=null)
									{
										degree += currNode.children.size();
									}
									System.out.println("Max degree of this node is: " + degree);
									System.out.println("Children nodes are:");
									for(int i : currNode.children)
										if(i!=currNode.UID)
											System.out.print(" " + i);
									System.out.println("\n");

									HashMap<Integer, Integer> childMaxDegreeMap = msg.childMaxDegree;
									if(childMaxDegreeMap!=null)
									{
										for(int uid: childMaxDegreeMap.keySet())
										{
											currNode.childMaxDegree.put(uid, childMaxDegreeMap.get(uid));
										}
									}
									
									currNode.childMaxDegree.put(currNode.UID, degree);
									if(parentId!=-1)
									{
										sendPositiveAck(parentId, Math.max(degree, currNode.degree),currNode.childMaxDegree);
									}

									if(parentId==-1)
									{	
										System.out.println("Max degree of all nodes: ");
										for(int uid: currNode.childMaxDegree.keySet())
										{
											System.out.print("\nUID: " + uid + " , MaxDegree: " + currNode.childMaxDegree.get(uid) + " ");
										}
									}
									currNode.closeSocketFlag=true;
								}
							}
					bfsBuffer.remove(msg);
				}
			}
		}
	}

	private void sendPositiveAck(int parentId, int maxDegree, HashMap<Integer, Integer> childMaxDegreeMap) {
		Node node = Node.getConfigMap().get(parentId);
		try {
			Socket st = new Socket(node.host, node.port);
			ObjectOutputStream oStream = new ObjectOutputStream(st.getOutputStream());

			//System.out.println("Positive Ack sent from " + currNode.UID + " to " +  parentId);
			Msg negAckMsg = new Msg(maxDegree, true, currNode.UID, true, true,childMaxDegreeMap);
			oStream.writeObject(negAckMsg);
			st.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void sendNegAck(Msg msg) {

		List<Msg> bfsBuffer = currNode.getBfsBuffer();
		synchronized (bfsBuffer) {
			int senderUID = msg.getSenderUID();
			Node node = Node.getConfigMap().get(senderUID);

			try {
				Socket st = new Socket(node.host, node.port);
				ObjectOutputStream oStream = new ObjectOutputStream(st.getOutputStream());
				//System.out.println("Negative ack send to " + senderUID  + " by " + currNode.UID);
				Msg negAckMsg = new Msg(0, true, senderUID, true, true,null);
				oStream.writeObject(negAckMsg);
				st.close();
				//bfsBuffer.remove(msg);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void sendNegAckToall() {
		List<Msg> bfsBuffer = currNode.getBfsBuffer();
		synchronized (bfsBuffer) {
			for (Iterator<Msg> iterator = bfsBuffer.iterator(); iterator.hasNext();) {
				Msg msg = iterator.next();

				if(msg.bfsMsgFlag && !msg.messageTypeAck)
				{
					int senderUID = msg.getSenderUID();
					Node node = Node.getConfigMap().get(senderUID);

					try {
						Socket st = new Socket(node.host, node.port);
						ObjectOutputStream oStream = new ObjectOutputStream(st.getOutputStream());
						Msg negAckMsg = new Msg(0, true, senderUID, true, true, null);
						oStream.writeObject(negAckMsg);
						st.close();
						bfsBuffer.remove(msg);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}

				}
			}
		}
	}

	private void sendSearchMsgtoAll() {
		for (int neighbor : currNode.getNeighbors()) {
			//System.out.println("Search Message sent by:" + currNode.UID + " to " + neighbor);
			Node node = Node.getConfigMap().get(neighbor);
			try {
				Socket st = new Socket(node.host, node.port);
				ObjectOutputStream oStream = new ObjectOutputStream(st.getOutputStream());

				// Ack message=false, i.e it is search message
				Msg msg = new Msg(currNode.UID, false, true);
				oStream.writeObject(msg);
				st.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


}
