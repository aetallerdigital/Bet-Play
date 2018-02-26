package com.betandplay.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.javalin.Context;

/*
 * 		Bet&Play Authentication flow a.k.a how Bet&Play will handle REST API security.
 * 
 * 	1. Client click the Login using facebook button in the app and is redirect to the authentication callback of the API with a code. 
 * 	2. Code sent to the authentication callback is then sent to facebook API 
 * 	3. Facebook API send back an access token
 * 	4. Access token is then sent to an facebook enpoint to check if it's valid
 * 	5. Access token is used to generate a JSON Web Token (JWT)
 * 	6. JWT is sent back to client
 * 	7. Client use the JWT previously sent to authenticate every request made to the API (except for loggin endpoints and api health check)
 */

public class AuthOld {

	public static final String CLIENT_ID = "1524904854243931";
	public static final String CLIENT_SECRET = "11819a916fe90eeab97f0f4cbe64b1da";
	public static final String REDIRECT_URI = "http://localhost:8080/auth";
	public static String APP_TOKEN = "1524904854243931|MDV5Z6bZbie-oitwqMtGNcjLvZc";
	
	public static ArrayList<UUID> tokens = new ArrayList<UUID>();
	
	private static ArrayList<String> fbTokens = new ArrayList<String>();

	public static String getAppToken() {
		String rq = "https://graph.facebook.com/oauth/access_token?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&grant_type=client_credentials";
		StringBuffer sb = new StringBuffer();
		try {
			HttpsURLConnection con = (HttpsURLConnection) new URL(rq).openConnection();
			if(con.getResponseCode() != 200) {

				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
				String l = "";
				while((l = reader.readLine()) != null)
					sb.append(l);

			} else {
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String l = "";
				while((l = reader.readLine()) != null)
					sb.append(l);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static String handleAuth(Context ctx) {

		String code = ctx.queryParam("code");
		if(code == null || code.isEmpty()) {
			ctx.redirect("https://www.facebook.com/dialog/oauth?client_id="+CLIENT_ID+"&redirect_uri="+REDIRECT_URI);
			return null;
		}

		JsonObject accessToken = getAccessToken(code);
		if(accessToken.get("error") != null) {
			ctx.result(accessToken.toString());
			return null;
		}
		
		String aToken = accessToken.get("access_token").getAsString();
		if(fbTokens.contains(aToken))
			return null;
		fbTokens.add(aToken);
		
		JsonObject checkedToken = checkAccessToken(accessToken.get("access_token").getAsString());
		ctx.result(accessToken.toString() + "\n" + checkedToken.toString());
		
		JsonObject checkedData = checkedToken.get("data").getAsJsonObject();
		
		if(checkedData.get("app_id").getAsString().equals(CLIENT_ID) && checkedData.get("is_valid").getAsBoolean())
			return generateToken();
		
		return null;

	}

	public static JsonObject checkAccessToken(String token) {
		String rq = "https://graph.facebook.com/debug_token?input_token=" + token + "&access_token=" + APP_TOKEN;
		StringBuffer sb = new StringBuffer();
		try {
			HttpsURLConnection con = (HttpsURLConnection) new URL(rq).openConnection();
			if(con.getResponseCode() != 200) {

				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
				String l = "";
				while((l = reader.readLine()) != null)
					sb.append(l);

			} else {
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String l = "";
				while((l = reader.readLine()) != null)
					sb.append(l);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new JsonParser().parse(sb.toString()).getAsJsonObject();
	}

	private static JsonObject getAccessToken(String code) {
		String rq = "https://graph.facebook.com/v2.12/oauth/access_token?"
				+ "client_id="+CLIENT_ID+"&redirect_uri="+REDIRECT_URI+"&client_secret="+CLIENT_SECRET+"&code="+code;
		StringBuffer sb = new StringBuffer();
		try {
			HttpsURLConnection con = (HttpsURLConnection) new URL(rq).openConnection();
			if(con.getResponseCode() != 200) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
				String l = "";
				while((l = reader.readLine()) != null)
					sb.append(l);
				return new JsonParser().parse(sb.toString()).getAsJsonObject();
			} else {
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String l = "";
				while((l = reader.readLine()) != null)
					sb.append(l);
				return new JsonParser().parse(sb.toString()).getAsJsonObject();
			}
		} catch (IOException e) {
			System.out.println("Error while processing authentification");
			e.printStackTrace();
			return null;
		}
	}
	
	public static String generateToken() {
		UUID token = UUID.randomUUID();
		tokens.add(token);
		return token.toString();
	}

	public static boolean check(String token) {
		
		if(token == null || token.equals(""))
			return false;
		
		UUID id;
		try {
			id = UUID.fromString(token);
		} catch(IllegalArgumentException e) {
			return false;
		}
		
		return tokens.contains(id);
	}

}
