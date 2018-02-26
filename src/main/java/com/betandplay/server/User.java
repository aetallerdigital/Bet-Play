package com.betandplay.server;

import java.util.HashMap;

import org.bson.Document;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.FindIterable;

public class User {
	
	public String fbID;
	public String name;
	public float money;
	public HashMap<String, Float> history;

	public User(String raw) {
		JsonObject json = new JsonParser().parse(raw).getAsJsonObject();
		this.fbID = json.get("fbID").getAsString();
		this.name = json.get("name").getAsString();
		this.money = json.get("money").getAsFloat();
		this.history = API.gson.fromJson(json.get("history"), new TypeToken<HashMap<String, Float>>(){}.getType());
	}
	
	public User(String fbID, String name, float money, HashMap<String, Float> history) {
		this.fbID = fbID;
		this.name = name;
		this.money = money;
		this.history = history;
	}
	
	public User(String fbID, String name) {
		this(fbID, name, 0.0f, new HashMap<String, Float>());
	}
	
	public Document getDocument() {
		return new Document("fbID", fbID)
				.append("name", name)
				.append("money", money)
				.append("history", history);
	}
	
	public static User findUserById(String fbID) {	
		FindIterable<Document> match = DB.users.find(new Document("fbID", fbID));
		return new User(match.first().toJson());
	}

	public static User createUser(String fbID, String name) {
		User u = new User(fbID, name);
		DB.users.insertOne(u.getDocument());
		return u;
	}
	
}
