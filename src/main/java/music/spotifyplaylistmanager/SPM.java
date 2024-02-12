package music.spotifyplaylistmanager;

import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.util.Base64;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.File;
import java.io.FileWriter;

public class SPM {
	public static void main(String[] args){
		getPlaylistTracks("6zbr9annOmLVvxWOfM3SBM");
		
		
		
		

		




		
	}

	public static void writeToFile(String json){
		try(FileWriter fw = new FileWriter(new File("tracks.txt"))){
			fw.write(json);
			fw.close();
		} catch (Exception e){}

	}

	public static String getRequestResponse(String url){
		String response = "asd";

		try{
			URL urlObj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) urlObj.openConnection();
		
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization","Bearer " + getToken());

			InputStream is = con.getInputStream();

			response = new String(is.readAllBytes());

		
		} catch (Exception e){}


		return (response);

	}

	public static JSONArray getPlaylistTracks(String playlistID){
		int offSet = 50;
		int limit = 50;
		JSONArray allTracks = new JSONArray();
		String response = getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?limit=50");
		JSONObject responseJSON = new JSONObject (response);
		allTracks.putAll(responseJSON.getJSONArray("items"));
		
		int size = responseJSON.getInt("total");

		while(offSet < size){
			System.out.println("a");
			response = getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?limit=50&offset=" + offSet);
			responseJSON = new JSONObject (response);
			allTracks.putAll(responseJSON.getJSONArray("items"));

			offSet += 50;
		}

		writeToFile(allTracks.toString());


		return (allTracks);
	}

	public static String getTrackInfo(String trackID){
		return (getRequestResponse("https://api.spotify.com/v1/tracks/" + trackID));
	}

	public static String getToken(){
		String clientID = "c592a3c9e9b34be59a79bfe0b98aa6a1";
		String clientSecret = "ff0366ee8e63480f88add8208435ad74";
		String tempLogin = clientID + ":" + clientSecret;
		String login = Base64.getEncoder().encodeToString(tempLogin.getBytes());
		String response = "";
		JSONObject tokenJSON = new JSONObject();

		try {
			URL urlObj = new URL("https://accounts.spotify.com/api/token");
			HttpsURLConnection con = (HttpsURLConnection) urlObj.openConnection();

			con.setDoOutput(true);
			con.setDoInput(true);

			con.setRequestMethod("POST");
			con.setRequestProperty("Authorization","Basic " + login);
			con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

			String requestBody = "grant_type=client_credentials";

			try(OutputStream os = con.getOutputStream()){
				byte[] input = requestBody.getBytes("UTF-8");
				os.write(input);
			} catch (Exception e){}
		
			InputStream is = con.getInputStream();
			response = new String(is.readAllBytes());
	
			tokenJSON = new JSONObject(response);

			con.disconnect();
		} catch (Exception e){}

	
		return (tokenJSON.getString("access_token"));
	}

}
