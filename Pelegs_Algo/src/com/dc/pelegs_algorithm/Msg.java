package com.dc.pelegs_algorithm;

import java.io.Serializable;
import java.util.HashMap;

public class Msg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//flag to identify null message
	boolean nullMsgFlag;
	int x;
	int d;
	
	// if round != -1 its a peleg's algorithm message else it's not
	int round = -1;
	int senderUID;

	boolean bfsMsgFlag = false;
	
	//max degree of node in the bfs
	int maxDegree;
	
	// boolean acknowledgement for Accept and Reject
	boolean ack;
	
	// type of message: true = acknowledgement, false = search;
	boolean messageTypeAck;
	
	//hashmap of children max degree
	HashMap<Integer, Integer> childMaxDegree = new HashMap<>();
		
	public Msg(boolean nullMsgFlag) {
		super();
		this.nullMsgFlag = nullMsgFlag;
	}

	public Msg(int x, int d, int round, int UID) {
		super();
		this.x = x;
		this.d = d;
		this.round = round;
		this.nullMsgFlag=false;
		this.senderUID=UID;
	}

	public Msg(int maxDegree, boolean ack, int senderUID, boolean messageTypeAck, boolean bfsFlag, HashMap<Integer, Integer> childMaxDegree)
	{
		this.maxDegree = maxDegree;
		this.ack = ack;
		this.senderUID = senderUID;
		this.messageTypeAck = messageTypeAck;
		this.bfsMsgFlag = bfsFlag;
		this.childMaxDegree = childMaxDegree;
		
	}
	
	public Msg(int senderUID, boolean messageTypeAck, boolean bfsMsgFlag)
	{
		this.senderUID = senderUID;
		this.messageTypeAck = messageTypeAck;
		this.bfsMsgFlag = bfsMsgFlag;
	}
	
	public int getSenderUID() {
		return senderUID;
	}

	public void setSenderUID(int senderUID) {
		this.senderUID = senderUID;
	}

	public int getMaxDegree() {
		return maxDegree;
	}

	public void setMaxDegree(int maxDegree) {
		this.maxDegree = maxDegree;
	}

	public boolean isAck() {
		return ack;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getD() {
		return d;
	}

	public void setD(int d) {
		this.d = d;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public boolean isNullMsgFlag() {
		return nullMsgFlag;
	}

	public void setNullMsgFlag(boolean nullMsgFlag) {
		this.nullMsgFlag = nullMsgFlag;
	}

	@Override
	public String toString() {
		return ( "X:" + this.x + ",D:" + this.d + ",Round:" + this.round + ",Sender:" + this.senderUID);
	}
}
