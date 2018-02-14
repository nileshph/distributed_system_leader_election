package com.dc.pelegs_algorithm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;

/*
 * Thread to handle incoming connections to node socket
 * Till termination, it listens to socket and puts all the messages
 * to thread safe collection
 */
public class ClientManager implements Runnable{

	//object of type node on which this thread runs
	Node t;

	public ClientManager(Node t) {
		super();
		this.t = t;
	}

	@Override
	public void run() {

		// need to handle termination of this loop
		// after all message passing is done
		while(true)
		{
			ObjectInputStream in = null;

			try {
				Socket s = t.getServerSocket().accept();
				in = new ObjectInputStream(s.getInputStream());
				Msg msg = (Msg)in.readObject();
				t.getMsgBuffer().add(msg);
				s.close();
				System.out.println("Message received: " + msg.toString());

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		//System.out.println("Termination detected, stopping client manager thread");

		//runCleanUp();
	}

	public void runCleanUp() {
		/*
		 * clean up stub
		 */

		try {
			t.serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
