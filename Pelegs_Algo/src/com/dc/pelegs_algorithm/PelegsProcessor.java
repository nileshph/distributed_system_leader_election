package com.dc.pelegs_algorithm;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;

public class PelegsProcessor implements Runnable {

	Node t;

	public PelegsProcessor(Node t) {
		super();
		this.t = t;
	}

	@Override
	public void run() {

		while(!t.termination)
		{
			int currRound = t.getRound();
			if(currRound==0)
			{
				sendMsgToNeighbors();
				t.setRound(currRound + 1);
			}
			else
			{
				if(checkForAllmsg())
				{
					System.out.println("Processing round : " + t.getRound());
					//proceed with current round

					//get msg with max UID
					Msg y = getMaxUID();

					/*
					 * if for current round,
					 * max X is received is greater than the current
					 * X, then change current X and D and then broadcast 
					 * it to neightbors
					 */
					if(y.x > t.x)
					{
						t.b = false;
						t.x = y.getX();
						t.d = y.getD() + 1;
						sendMsgToNeighbors();
					}
					else
					{
						if(y.x < t.x)
						{
							t.c = 1;
							sendMsgToNeighbors();
						}

						else
							if(y.x == t.x)
							{
								int z = Math.max(t.d, y.d);

								if(z > t.d)
								{
									t.d = z;
									t.c = 0;
								}
								else
									if(z==t.d)
									{
										t.c = t.c + 1;

										/*
										 * detect the termination if c ==2
										 * Send termination msg to all neighbors
										 */
										if(t.c == 2)
											sendTerminationMsgtoAll();
									}
							}
					}
					t.setRound(t.round + 1);
				}
			}
		}

		//propogate termination message to one hop neighbors
		sendTerminationMsgtoAll();
	}

	private void sendMsgToNeighbors() {

		for(int neighbor: t.getNeighbors())
		{
			System.out.println("sending normal message for round " + t.round + " to neighbor UID " + neighbor);
			Node tt = Node.getConfigMap().get(neighbor);
			try {
				Socket st = new Socket(tt.host, tt.port);
				ObjectOutputStream oStream = new ObjectOutputStream(st.getOutputStream());

				Msg msg = new Msg(t.x, t.d, t.round,t.UID);
				oStream.writeObject(msg);
				st.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}

	void sendTerminationMsgtoAll() {

		for(int UID : t.getNeighbors())
		{
			System.out.println("Sending termination message to UID: " + UID);
			Node tt = Node.getConfigMap().get(UID);

			Msg terminationMsg = new Msg(t.x, -1, t.round+1,t.UID);

			try {
				Socket st = new Socket(tt.host, tt.port);
				ObjectOutputStream out = new ObjectOutputStream(st.getOutputStream());
				out.writeObject(terminationMsg);
				st.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	Msg getMaxUID() {

		int max = Integer.MIN_VALUE;
		Msg maxMsg = null;
		List<Msg> msgBuffer = t.getMsgBuffer();
		synchronized(msgBuffer)
		{
			for(Iterator<Msg> iterator = msgBuffer.iterator(); iterator.hasNext();)
			{
				Msg msg = iterator.next();
				if(msg!=null && msg.getX() > max && msg.getRound() == (t.round - 1))
				{
					max = msg.getX();
					maxMsg = msg;
				}
			}
			//remove all the messages with current round -1 which has x < max
			for(Iterator<Msg> iterator = msgBuffer.iterator(); iterator.hasNext();)
			{
				Msg msg = iterator.next();
				if(msg!=null && msg.getRound() == (t.round - 1) && msg.getX() < max)
					t.msgBuffer.remove(msg);
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
		List<Msg> msgBuffer = t.getMsgBuffer();
		synchronized(msgBuffer)
		{
			for(Iterator<Msg> iterator = msgBuffer.iterator(); iterator.hasNext();)
			{
				Msg msg = iterator.next();
				if(msg!=null && msg.getRound() == (t.getRound() -1))
				{
					cnt++;
					if(msg.getD() == -1) {
						t.termination = true;
						System.out.println("Leader Found-->" + msg.getX() +  " terminating Node with UID " + t.UID);
					}
				}
			}	
			if(cnt == t.getNeighbors().size())
				return true;

			//System.out.println("Not all messages available for round " + (t.getRound()-1));
			return false;
		}
	}

}
