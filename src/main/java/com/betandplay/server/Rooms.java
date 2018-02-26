package com.betandplay.server;

import java.util.HashMap;

public class Rooms {
	
	private static HashMap<String, Room> rooms = new HashMap<String, Room>();
	
	public static Room get(String key) {
		return rooms.get(key);
	}
	
	public static Room add(Room room) {
		if(rooms.put(room.getName(), room) == null)
			return room;
		else
			return null;
	}
	
	public static void remove(Room room) {
		rooms.remove(room.getName());
	}
	
}
