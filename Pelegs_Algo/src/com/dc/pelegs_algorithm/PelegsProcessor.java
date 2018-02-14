package com.dc.pelegs_algorithm;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;

public class PelegsProcessor implements Runnable {

	Node thisNode;

	public PelegsProcessor(Node t) {
		super();
		this.thisNode = t;
	}

	@Override
	public void run() {

		while(!thisNode.termination)
		{
			int currRound = thisNode.getRound();
			if(currRound==0)
			{
				sendMsgToNeighbors();
				thisNode.setRound(currRound + 1);
			}
			else
			{
				if(checkForAllmsg())
				{
					System.out.println("Processing round : " + thisNode.getRound());
					//proceed with current round

					int z = Integer.MIN_VALUE;
					
					/*
					 * for current round -1 
					 * get msg with max x, if this x = thisNode.x
					 * get max distance from all the messages for current round -1 
					 * and have x = thisNode.x, that's the z
					 */
					Msg y = getMaxUID(z);

					System.out.println("Max UID msg is" + y.toString());
					System.out.println("Current node status is X: " + thisNode.x + ", d:" + thisNode.d  + ",C:" + thisNode.c);
					/*
					 * if for the current round,
					 * max X is received is greater than the current
					 * X, then change current X and D and then broadcast 
					 * it to neighbors
					 */
					if(y.x > thisNode.x)
					{
						thisNode.b = false;
						thisNode.x = y.getX();
						
						// does this should be current round ?? as per the peleg's paper
						thisNode.d = y.getD() + 1;
						sendMsgToNeighbors();

						//do we need this ?? reset c counter if x changed
						//thisNode.c = 0;
					}
					else
					{
						if(y.x < thisNode.x)
						{
							thisNode.c = 1;
							sendMsgToNeighbors();
						}

						else
							if(y.x == thisNode.x)
							{
								if(z > thisNode.d)
								{
									thisNode.d = z;
									thisNode.c = 0;
									sendMsgToNeighbors();
								}
								else
									if(z==thisNode.d)
									{
										thisNode.c = thisNode.c + 1;

										/*
										 * detect the termination if c ==2
										 * Send termination message to all neighbors
										 */
										if(thisNode.c == 2)
										{
											System.out.println("Leader found at " +  thisNode.UID);
											sendTerminationMsgtoAll();
										}
											
										else
											sendMsgToNeighbors();
									}
							}
					}
					thisNode.setRound(thisNode.round + 1);
				}
			}
		}

		//propogate termination message to one hop neighbors
		sendTerminationMsgtoAll();
	}

	private void sendMsgToNeighbors() {

		for(int neighbor: thisNode.getNeighbors())
		{
			System.out.println("Sending Peleg's Message for Round: " + thisNode.round + " to neighbor with UID: " + neighbor);
			Node tt = Node.getConfigMap().get(neighbor);
			try {
				Socket st = new Socket(tt.host, tt.port);
				ObjectOutputStream oStream = new ObjectOutputStream(st.getOutputStream());

				Msg msg = new Msg(thisNode.x, thisNode.d, thisNode.round,thisNode.UID);
				oStream.writeObject(msg);
				st.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}

	void sendTerminationMsgtoAll() {

		for(int UID : thisNode.getNeighbors())
		{
			System.out.println("Sending the termination message to the UID: " + UID);
			Node tt = Node.getConfigMap().get(UID);

			Msg terminationMsg = new Msg(thisNode.x, -1, thisNode.round+1,thisNode.UID);

			try {
				Socket st = new Socket(tt.host, tt.port);
				ObjectOutputStream out = new ObjectOutputStream(st.getOutputStream());
				out.writeObject(terminationMsg);
				st.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	Msg getMaxUID(int maxDistance) {

		/*
		 * Function to get message with Max UID for the last round
		 * Any other messages for the last round with UID < Max UID will be
		 * deleted from the buffer
		 */
		int max = Integer.MIN_VALUE;
		Msg maxMsg = null;
		List<Msg> msgBuffer = thisNode.getMsgBuffer();
		synchronized(msgBuffer)
		{
			for(Iterator<Msg> iterator = msgBuffer.iterator(); iterator.hasNext();)
			{
				Msg msg = iterator.next();
				if(msg!=null && msg.getX() > max && msg.getRound() == (thisNode.round - 1))
				{
					max = msg.getX();
					maxMsg = msg;
				}
			}
			
			/*
			 * remove all those messages which has X < maxMsg.x
			 * out of all remaining messages for thisNode.round - 1
			 * get Max of d
			 */
			for(Iterator<Msg> iterator = msgBuffer.iterator(); iterator.hasNext();)
			{
				Msg msg = iterator.next();
				if(msg!=null && msg.getRound() == (thisNode.round - 1) && msg.getX() < max)
					thisNode.msgBuffer.remove(msg);
				else
					if(msg!=null && msg.getRound() == (thisNode.round -1) && msg.getX() == max)
						if(maxDistance < msg.getD())
							maxDistance = msg.getD();
			}
			return maxMsg;
		}
	}

	/*
	 * check if node received messages from all of its neighbors
	 * for the previous round
	 */
	private  boolean checkForAllmsg() {

		int cnt = 0;
		List<Msg> msgBuffer = thisNode.getMsgBuffer();
		synchronized(msgBuffer)
		{
			for(Iterator<Msg> iterator = msgBuffer.iterator(); iterator.hasNext();)
			{
				Msg msg = iterator.next();
				if(msg!=null && msg.getRound() == (thisNode.getRound() -1))
				{
					cnt++;
					if(msg.getD() == -1) {
						thisNode.termination = true;
						System.out.println("Leader Found-->" + msg.getX() +  " terminating Node with UID " + thisNode.UID);
					}
				}
			}	
			if(cnt == thisNode.getNeighbors().size())
				return true;

			//System.out.println("Not all messages available for round " + (t.getRound()-1));
			return false;
		}
	}

}
