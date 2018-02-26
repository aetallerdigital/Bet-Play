package com.betandplay.server;

import io.javalin.*;
import com.betandplay.server.authetication.*;

public class Router {
	
	public Router() {
		
		//Server configuration;
		
		Javalin app = Javalin.create();
		
		app.port(8080);
		
		//Setting up routes
		
		app.get("/", ctx -> {
			
		});
		
		/*
		 * Get api status and version
		 */
		app.get("/api", ctx -> {
			
			if(!Auth.authenticateRequest(ctx.header("Authorization"))) {
				ctx.status(403);
				ctx.json(new Error("Forbiden"));
				return;
			}
				
			ctx.status(403);
			ctx.result("{ \"version\":\""+ API.VERSION + "\" }");
			
		});
		
		app.get("/auth", ctx -> {
			
			/*String auth = Auth.handleAuth(ctx);
			if(auth == null) {
				ctx.json(new Error("Error while auth"));
				return;
			}
		
			ctx.result(auth);*/
			
			Auth.authenticationCallback(ctx);
			
		});
		
		/*
		 * TEMP - list all tokens
		 */
		app.get("/tokens", ctx -> {

			//ctx.json(Auth.tokens);
		});
		
		/*
		 * Create a room with the given name and return it's JSON-representation.
		 */
		app.get("/room/create/:name", ctx -> {
			
			String owner = ctx.queryParam("owner");
			if(owner == null || owner.equals("")) {
				ctx.json(new Error("Owner could not be null."));
				return;
			}
			
			ctx.result(Rooms.add(new Room(ctx.param("name"), ctx.queryParam("owner"))).toString());
			
		});
		
		/*
		 * Close room for future bets
		 */
		app.get("/room/close/:name", ctx -> {
			
			
			
		});
		
		/*
		 * Return the JSON representation of the requested room
		 */
		app.get("/room/get/:name", ctx -> {
			
			Room r = Rooms.get(ctx.param("name"));
			if(r != null) {
				ctx.result(r.toString());
				return;
			}
			
			ctx.json(new Error("Could not find room."));
			
		});
		
		//Starting server
		app.start();
		
	}
	
}
