package com.dc.pelegs_algorithm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/*
 * Thread to handle incoming connections to node socket
 * Till termination, it listens to socket and puts all the messages
 * to thread safe collection
 */
public class ClientManager implements Runnable{

	//object of type node on which this thread runs
	Node thisNode;

	public ClientManager(Node t) {
		super();
		this.thisNode = t;
	}

	@Override
	public void run() {

		while(!thisNode.closeSocketFlag)
		{
			ObjectInputStream in = null;

			try {
				Socket s = thisNode.getServerSocket().accept();
				in = new ObjectInputStream(s.getInputStream());
				Msg msg = (Msg)in.readObject();
				thisNode.getMsgBuffer().add(msg);
				thisNode.getBfsBuffer().add(msg);
				s.close();

				System.out.println("Message received: " + msg.toString());

				if(msg.getD() == -1)
				{
					System.out.println("Leader elected with UID: " + msg.getX());
					thisNode.terminatePelegsFlag = true;
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		//runCleanUp();
	}

	public void runCleanUp() {
		/*
		 * clean up stub
		 */

		try {
			thisNode.serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
