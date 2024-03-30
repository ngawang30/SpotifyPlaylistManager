package music.spotifyplaylistmanager;

import java.net.URL;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Image;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import org.json.JSONObject;
import org.json.JSONArray;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.awt.Color;
import javax.swing.BorderFactory;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Component;
import java.awt.Point;
import java.util.List;
import java.util.Arrays;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import java.util.Locale;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import java.io.BufferedReader;
import java.net.URLEncoder;
import org.openqa.selenium.NoSuchWindowException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JTextArea;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class APIHandler {
	
	//MusicBrainz
	public static void loadMusicBrainz(JSONArray in){
		int counter = 0;
		int rate = 1000;
		
		try{
			for(int i = 0; i < in.length(); i++){
				//Request 1
				
				JSONObject currentTrack = in.getJSONObject(i);
				String query = "query=" + URLEncoder.encode("ANDrecording:\"" + (currentTrack.getString("trackName") + "\"ANDartist:\"" + currentTrack.getString("trackArtist") + "\"ANDrelease:\"" + currentTrack.getString("album")) + "\"","UTF-8");
				
				HttpRequest musicBrainzRequest = HttpRequest.newBuilder()
					.uri(new URI("https://musicbrainz.org/ws/2/recording?" + query))
					.headers("Accept","application/json","User-Agent","SpotifyPlaylistManager ( ngawang30@gmail.com )")
					.GET()
					.build();
				
				HttpResponse<String> response = HttpClient.newHttpClient().send(musicBrainzRequest,HttpResponse.BodyHandlers.ofString());
				JSONObject newBrainz = new JSONObject(response.body());
				
				String brainzRecordingID = newBrainz.getJSONArray("recordings").getJSONObject(0).getString("id");
				in.getJSONObject(i).put("brainzRecordingID",brainzRecordingID);

				String brainzArtistID = newBrainz.getJSONArray("recordings").getJSONObject(0).getJSONArray("artist-credit").getJSONObject(0).getJSONObject("artist").getString("id");
				in.getJSONObject(i).put("brainzArtistID",brainzArtistID);
				
				
				//API LimitCheck
				Thread.sleep(rate);
				
				
				//Request 2
				 musicBrainzRequest = HttpRequest.newBuilder()
					.uri(new URI("https://musicbrainz.org/ws/2/artist/" + brainzArtistID))
					.headers("Accept","application/json","User-Agent","SpotifyPlaylistManager ( ngawang30@gmail.com )")
					.GET()
					.build();
				
				response = HttpClient.newHttpClient().send(musicBrainzRequest,HttpResponse.BodyHandlers.ofString());
				newBrainz = new JSONObject(response.body());
				
				String artistType = newBrainz.optString("type","null");
				in.getJSONObject(i).put("artistType",artistType);
				
				String artistCountry = newBrainz.isNull("area")?"null":newBrainz.getJSONObject("area").getString("name");
				in.getJSONObject(i).put("artistCountry",artistCountry);
				
				String artistGender = newBrainz.isNull("gender")?"null":newBrainz.getString("gender");
				in.getJSONObject(i).put("artistGender",artistGender);
				
				String isDead = newBrainz.isNull("life-span")?"null":String.valueOf(newBrainz.getJSONObject("life-span").getBoolean("ended"));
				in.getJSONObject(i).put("isDead",isDead);
				
				String subArea = newBrainz.isNull("begin_area")?"null":newBrainz.getJSONObject("begin_area").getString("name");
				in.getJSONObject(i).put("subArea",subArea);
				
				//API LimitCheck
				Thread.sleep(rate);
				
				
				//request 3
				musicBrainzRequest = HttpRequest.newBuilder()
					.uri(new URI("https://musicbrainz.org/ws/2/recording/" + brainzRecordingID + "?inc=releases"))
					.headers("Accept","application/json","User-Agent","SpotifyPlaylistManager ( ngawang30@gmail.com )")
					.GET()
					.build();
				
				response = HttpClient.newHttpClient().send(musicBrainzRequest,HttpResponse.BodyHandlers.ofString());
				newBrainz = new JSONObject(response.body());
				
				JSONObject languageRoot = newBrainz.getJSONArray("releases").getJSONObject(0).getJSONObject("text-representation");
				String language = languageRoot.isNull("language")?"null":(new Locale(newBrainz.getJSONArray("releases").getJSONObject(0).getJSONObject("text-representation").getString("language"))).getDisplayLanguage();
				in.getJSONObject(i).put("language",language);
				
				//API LimitCheck
				Thread.sleep(rate);
				
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	//Spotify
	public static String getRequestResponse(String url){
		String response = "error";

		try{
			HttpRequest generalRequest = HttpRequest.newBuilder()
				.uri(new URI(url))
				.headers("Authorization", "Bearer " + getToken())
				.GET()
				.build();
				
			HttpResponse<String> responseBody = HttpClient.newHttpClient().send(generalRequest,HttpResponse.BodyHandlers.ofString());
			response = responseBody.body();
			
		} catch (Exception e){}

		return (response);
	}
	
	
	public static String getRequestResponse(String url, String token){
		String response = "error";

		try{
			HttpRequest generalRequest = HttpRequest.newBuilder()
				.uri(new URI(url))
				.headers("Authorization", "Bearer " + token)
				.GET()
				.build();
				
			HttpResponse<String> responseBody = HttpClient.newHttpClient().send(generalRequest,HttpResponse.BodyHandlers.ofString());
			response = responseBody.body();
		} catch (Exception e){}

		return (response);
	}
	
	public static String getToken(){
		String clientID = "c592a3c9e9b34be59a79bfe0b98aa6a1";
		String clientSecret = "ff0366ee8e63480f88add8208435ad74";
		String tempLogin = clientID + ":" + clientSecret;
		String login = Base64.getEncoder().encodeToString(tempLogin.getBytes());
		byte [] requestBody = "grant_type=client_credentials".getBytes();
		HttpResponse<String> response = null;
		
		try{
			HttpRequest tokenRequest = HttpRequest.newBuilder()
				.uri(new URI("https://accounts.spotify.com/api/token"))
				.headers("Authorization", "Basic " + login, "Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
				.build();
			
			response = HttpClient.newHttpClient().send(tokenRequest,HttpResponse.BodyHandlers.ofString());
		} catch (Exception e){}
		
		JSONObject tokenJSON = new JSONObject(response.body());
		
		return (tokenJSON.getString("access_token"));
	}
	
	public static String getAuthorizationCode(){
		
		String site = "https://accounts.spotify.com/authorize?client_id=c592a3c9e9b34be59a79bfe0b98aa6a1&redirect_uri=http://localhost/&response_type=code&scope=user-read-email,user-read-private,playlist-modify-public,playlist-modify-private,playlist-read-private,user-read-playback-state,user-modify-playback-state,user-read-currently-playing,";
		WebDriver driver = new ChromeDriver();
		driver.get(site);
		String authorizationCode = "";
		
		SwingWorker sw = new SwingWorker(){
				@Override
				protected String doInBackground(){
					try{
						while(driver.getTitle()!=null){
						}
					} catch (NoSuchWindowException e){
						driver.quit();
					}

					return("");
				}
				
				@Override
				protected void done(){}
				
		};
		
		sw.execute();
		
		new WebDriverWait(driver, Duration.ofMinutes(2)).until(ExpectedConditions.urlContains("localhost/?code"));
		
		authorizationCode = driver.getCurrentUrl();
		authorizationCode = authorizationCode.substring(authorizationCode.indexOf("=")+1);
		driver.quit();
		
		return(authorizationCode);
	}
	
	public static String getAuthorizedToken(){
		
		String clientID = "c592a3c9e9b34be59a79bfe0b98aa6a1";
		String clientSecret = "ff0366ee8e63480f88add8208435ad74";
		String tempLogin = clientID + ":" + clientSecret;
		String login = Base64.getEncoder().encodeToString(tempLogin.getBytes());
		HttpResponse<String> response = null;
		String authorizationCode = getAuthorizationCode();
		
		byte [] requestBody = new String("grant_type=authorization_code&code=" + authorizationCode + "&redirect_uri=http://localhost/").getBytes();
		
		try{
			HttpRequest tokenRequest = HttpRequest.newBuilder()
				.uri(new URI("https://accounts.spotify.com/api/token"))
				.headers("Authorization", "Basic " + login, "Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
				.build();
				
			HttpClient tokenClient = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.ALWAYS)
				.build();
			
			response = tokenClient.send(tokenRequest,HttpResponse.BodyHandlers.ofString());
		} catch (Exception e){}
		
		String token = new JSONObject(response.body()).getString("access_token");
		
		return (token);
	}
	
	public static String getAuthorizedToken(String refreshToken){
		String clientID = "c592a3c9e9b34be59a79bfe0b98aa6a1";
		String clientSecret = "ff0366ee8e63480f88add8208435ad74";
		String tempLogin = clientID + ":" + clientSecret;
		String login = Base64.getEncoder().encodeToString(tempLogin.getBytes());
		HttpResponse<String> response = null;
		
		byte [] requestBody = new String("grant_type=refresh_token&refresh_token=" + refreshToken).getBytes();
		
		try{
			HttpRequest tokenRequest = HttpRequest.newBuilder()
				.uri(new URI("https://accounts.spotify.com/api/token"))
				.headers("Authorization", "Basic " + login, "Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
				.build();
				
			HttpClient tokenClient = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.ALWAYS)
				.build();
			
			response = tokenClient.send(tokenRequest,HttpResponse.BodyHandlers.ofString());
		} catch (Exception e){}
		
		String token = new JSONObject(response.body()).getString("access_token");
		
		return (token);
	}
	
	
	
}