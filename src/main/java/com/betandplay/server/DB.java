package com.betandplay.server;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

public class DB {
	
	public static MongoClient client;
	public static MongoDatabase db;
	public static MongoCollection<Document> users;
	
	public static void initDB() {
		client = new MongoClient("89.234.183.252", 27017);
		db = client.getDatabase("betandplay");
		users = db.getCollection("users");
	}

}
