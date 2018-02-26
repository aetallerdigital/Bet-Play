package com.betandplay.server;

import java.util.ArrayList;
import java.util.HashMap;

public class Room {

	private String name;
	private String owner;
	private String winner;
	private boolean closed;
	private ArrayList<String> users = new ArrayList<String>();
	private HashMap<String, Float> bets = new HashMap<String, Float>();
	
	public Room(String name, String owner) {
		this.name = name;
		this.owner = owner;
		this.users = new ArrayList<String>();
		users.add(owner);
	}
	
	public void setWinner(String winner) {
		this.winner = winner;
	}
	
	public void setClosed(boolean b) {
		this.closed = b;
	}
	
	public boolean isClosed() {
		return closed;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return API.gson.toJson(this);
	}
	
}
