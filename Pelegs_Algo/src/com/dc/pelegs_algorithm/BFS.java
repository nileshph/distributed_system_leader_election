package com.dc.pelegs_algorithm;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;

public class BFS implements Runnable {

	Node currNode;

	public BFS(Node thisNode) {
		this.currNode = thisNode;
	}

	@Override
	public void run() {

		boolean searchMessagesSent = false;

		while (!currNode.isAllChildrenFound()) {
			// Leader send search message to all neighbors
			if (currNode.UID == currNode.x && currNode.isLeader && !searchMessagesSent) {
				System.out.println("leader is" + currNode.x);
				currNode.marked = true;
				currNode.parent = Integer.MAX_VALUE;
				sendSearchMessage();
				searchMessagesSent = true;
			}

			// unmarked node check for all received search messages, mark
			// themselves & mark one of process
			// from which it has received message as its parent
			checkForAllmsg(false);

			// If a node get marked, send messages to all its neighbors
			if (currNode.isMarked() && !searchMessagesSent) {
				sendSearchMessage();
				int messagesSent = currNode.getNeighbors().size();
				System.out.println("Total messages sent to neighbors" + messagesSent);
				searchMessagesSent = true;
			}

			checkForAllmsg(true); // check for all the acknowledgments
			// If received  acknowledgement from all its neighbors, send ack to parent
			if (currNode.ackCount == currNode.getNeighbors().size()) {

				if(currNode.getDegree() > currNode.maxDegree)
				{
					currNode.maxDegree = currNode.getDegree();
					currNode.maxDegreeUID = currNode.UID;
				}
				//currNode.maxDegree = currNode.maxDegree>currNode.getDegree()?currNode.maxDegree:currNode.getDegree();
				System.out.println("all ack found, my parent is: " + currNode.parent);
				System.out.println("In degree: " + currNode.getDegree());
				System.out.println("My children list: ");

				for (int x : currNode.getChildren()) {
					System.out.println(x);
				}

				if (!currNode.isLeader) {
					//					System.out.println("Max degree seen so far: " + currNode.maxDegree);
					System.out.println("message to notify parent: ");
					sendAcknowlegment(Node.getConfigMap().get(currNode.parent), true);
					currNode.setAllChildrenFound(true);
				} else {
					System.out.println("Max degree of bfs tree is: " + currNode.maxDegree);
					System.out.println("Max Degree UID is: " + currNode.maxDegreeUID);
					currNode.setAllChildrenFound(true);
				}
			}
		}
		System.out.println("Proess ENDS !");
	}

	private void sendSearchMessage() {

		for (int neighbor : currNode.getNeighbors()) {
			System.out.println("Message sent by:" + currNode.UID);
			Node tt = Node.getConfigMap().get(neighbor);

			try {
				Socket st = new Socket(tt.host, tt.port);
				ObjectOutputStream oStream = new ObjectOutputStream(st.getOutputStream());
				tt.setMarked(true);
				Msg msg = new Msg(currNode.UID, false);
				oStream.writeObject(msg);
				st.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendAcknowlegment(Node tt, boolean acknowledgement) {

		try {
			System.out.println("Message sent by:" + currNode.UID + " to Node: " + tt.UID);
			Socket st = new Socket(tt.host, tt.port);
			ObjectOutputStream oStream = new ObjectOutputStream(st.getOutputStream());
			Msg msg = new Msg(currNode.maxDegree, acknowledgement, currNode.UID, true);
			oStream.writeObject(msg);
			st.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void checkForAllmsg(boolean messageTypeAck) {

		List<Msg> bfsBuffer = currNode.getBfsBuffer();
		synchronized (bfsBuffer) {
			for (Iterator<Msg> iterator = bfsBuffer.iterator(); iterator.hasNext();) {
				Msg msg = iterator.next();
				Node tt = Node.getConfigMap().get(msg.senderUID);
				if (msg.messageTypeAck == messageTypeAck && msg != null) {
					if (msg.messageTypeAck == false && !currNode.isMarked()) {
						currNode.setParent(msg.senderUID);
						currNode.setMarked(true);
					} 
					else if (msg.messageTypeAck == false) // if node is already marked, send -ve ack immediately							
						sendAcknowlegment(tt, false);

					if (msg.messageTypeAck == true) {
						currNode.ackCount += 1;
						if (msg.isAck()) {
							currNode.setDegree(currNode.getDegree() + 1);
							System.out.println(currNode.UID + " " + currNode.maxDegree + " " + tt.UID + " " + tt.maxDegree);

							//update max-degree for current node
							if( msg.getMaxDegree()>currNode.maxDegree )
							{
								currNode.maxDegree = msg.getMaxDegree();
								currNode.maxDegreeUID = msg.getSenderUID();
							}

							System.out.println("child to be added:" + msg.getSenderUID());
							//if (!currNode.getChildren().contains(msg.getSenderUID()))
							currNode.getChildren().add(msg.getSenderUID());
						}
					}
					bfsBuffer.remove(msg);
				}
			}
		}
	}
}
