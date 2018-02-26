package com.betandplay.server;

import java.security.Key;

import io.jsonwebtoken.impl.crypto.MacProvider;

public class Main {

	public static void main(String[] args) {
		
		Key key = MacProvider.generateKey();
		System.out.println(key.getEncoded());
		
		DB.initDB();
		
		Router router = new Router();
		
	}

}
