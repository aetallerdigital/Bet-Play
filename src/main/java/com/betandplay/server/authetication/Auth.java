package com.betandplay.server.authetication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.betandplay.server.API;
import com.betandplay.server.DB;
import com.betandplay.server.Error;
import com.betandplay.server.User;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import io.javalin.Context;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

/*
 * 		Bet&Play Authentication flow a.k.a how Bet&Play will handle REST API security.
 * 
 * 	1. Client click the Login using facebook button in the app and is redirect to the authentication callback of the API with a code. 
 * 	2. Code sent to the authentication callback is then sent to facebook API 
 * 	3. Facebook API send back an access token
 * 	4. Access token is then sent to an facebook enpoint to check if it's valid
 * 	5. Access token is used to get the user profile from database
 * 	6. Access token is used to generate a JSON Web Token (JWT) usig the user id
 * 	7. JWT is sent back to client
 * 	8. Client use the JWT previously sent to authenticate every request made to the API (except for loggin endpoints and api health check)
 */

public class Auth {

	private static final String APP_ID = "1524904854243931";
	private static final String APP_SECRET = "11819a916fe90eeab97f0f4cbe64b1da";
	private static final String APP_REDIRECT_URI = "http://localhost:8080/auth/";
	private static final String APP_TOKEN = "1524904854243931|MDV5Z6bZbie-oitwqMtGNcjLvZc";

	private static Gson gson = new Gson();

	/*
	 * Step 1, 7.
	 */
	public static void authenticationCallback(Context ctx) {

		String code = ctx.queryParam("code");
		if(code == null || code.isEmpty()) {
			ctx.status(401);
			ctx.json(new Error("Invalid code"));
			return;
		}

		String authToken = getAuthenticationToken(code);
		if(authToken == null) {
			ctx.status(401);
			ctx.json(new Error("Error while processing auth token"));
			return;
		}

		boolean b = checkAuthenticationToken(authToken);
		if(!checkAuthenticationToken(authToken)) {
			ctx.status(401);
			ctx.json(new Error("Access token verification failed"));
			return;
		}

		//Get fbID 
		String request = "https://graph.facebook.com/me?access_token="+authToken;
		JsonObject json = new JsonParser().parse(makeHttpRequest(request)).getAsJsonObject();
		if(json.get("error") != null) {
			ctx.status(401);
			ctx.json(new Error("Invalid access token"));
			return;
		}
		String id = json.get("id").getAsString();
		String name = json.get("name").getAsString();

		if(DB.users.count(new Document("fbID", id)) == 0)
			User.createUser(id, name);

		ctx.json(generateJWT(id));
		ctx.status(200);
	}

	/*
	 * Step 2, 3.
	 */
	public static String getAuthenticationToken(String code) {

		String request = "https://graph.facebook.com/v2.12/oauth/access_token?"
				+ "client_id="+APP_ID+"&redirect_uri="+APP_REDIRECT_URI+"&client_secret="+APP_SECRET+"&code="+code;
		try {
			return new JsonParser().parse(makeHttpRequest(request)).getAsJsonObject().get("access_token").getAsString();
		} catch(Exception e) {
			return null;
		}
	}

	/*
	 * Step 4.
	 */
	public static boolean checkAuthenticationToken(String token) {

		String request = "https://graph.facebook.com/debug_token?input_token=" + token + "&access_token=" + APP_TOKEN;

		//TODO: check expiration of access token ?

		try {
			JsonObject json = new JsonParser().parse(makeHttpRequest(request)).getAsJsonObject();
			JsonObject data = json.get("data").getAsJsonObject();
			
			if(data.get("error") != null)
				return false;
			
			Type type = new TypeToken<ArrayList<String>>(){}.getType();
			ArrayList<String> scopes = gson.fromJson(data.get("scopes").getAsJsonArray(), type);

			if(data.get("app_id").getAsString().equals(APP_ID) && data.get("is_valid").getAsBoolean() && scopes.contains("user_friends"))
				return true;
			else
				return false;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * Step 6.
	 */
	public static String generateJWT(String id) {

		String jwt = Jwts.builder()
				.setIssuer("Bet&Play")
				.claim("user_id", id)
				.signWith(SignatureAlgorithm.HS512, API.SIGN_KEY)
				.compact();

		return jwt;
	}

	/*
	 * Step 8.
	 */
	public static boolean authenticateRequest(String jwt) {
		if(jwt == null || jwt.isEmpty())
			return false;
		
		try {
			 Jwts.parser().setSigningKey(API.SIGN_KEY).parseClaimsJws(jwt);
			return true;
		} catch(Exception e) {
			return false;
		}
		
	}

	private static String makeHttpRequest(String request) {
		try {

			StringBuffer result = new StringBuffer();
			HttpURLConnection con = (HttpURLConnection) new URL(request).openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(	(con.getResponseCode() == 200) ? con.getInputStream() : con.getErrorStream()));
			String l;
			while((l = reader.readLine()) != null)
				result.append(l);

			return result.toString();

		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}

	}
}
