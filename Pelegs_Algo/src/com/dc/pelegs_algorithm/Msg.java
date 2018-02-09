package com.dc.pelegs_algorithm;
//package com.dc.pelegs_algorithm;


import java.io.Serializable;

public class Msg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//flag to identify null msg
	boolean nullMsgFlag;
	int x;
	int d;
	int round;

	public Msg(boolean nullMsgFlag) {
		super();
		this.nullMsgFlag = nullMsgFlag;
	}

	public Msg(int x, int d, int round) {
		super();
		this.x = x;
		this.d = d;
		this.round = round;
		this.nullMsgFlag=false;
	}

	public int getX() {
		return x;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return (this.nullMsgFlag + "|" + this.x + "|" + this.d + "|" + this.round);
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

}
