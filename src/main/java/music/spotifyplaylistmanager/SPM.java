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


public class SPM {
	public static void main(String[] args) throws Exception{
		app();
		
	}
	
	public static void app (){
		//Playlist Body Component
		PlaylistManager man = new PlaylistManager();
		man.token = getToken();
		
		JPanel playlistContainer = new JPanel();
		playlistContainer.setLayout(new BoxLayout(playlistContainer,BoxLayout.Y_AXIS));

		JScrollPane playlistContainerScroll = new JScrollPane();
		playlistContainerScroll.setViewportView(playlistContainer);

		//Menu Component //Playlist Menu
		JMenuItem importSpotifyMenuItem = new JMenuItem("Import Playlist From Spotify");
		importSpotifyMenuItem.addActionListener(e -> promptPlaylistID(playlistContainer, man));

		JMenuItem importJSONMenuItem = new JMenuItem("Import Playlist From JSON");
		importJSONMenuItem.addActionListener(e -> loadPlaylistFromJSON(playlistContainer, man));


		JMenuItem exportJSONMenuItem = new JMenuItem("Export Playlist to JSON");
		exportJSONMenuItem.addActionListener(e -> exportToJSON(man));
		
		JMenuItem exportSpotifyMenuItem = new JMenuItem("Export Playlist to Spotify");
		exportSpotifyMenuItem.addActionListener(e -> uploadPlaylist(man));
		
		JMenu playlistMenu = new JMenu("Playlist");
		playlistMenu.add(importSpotifyMenuItem);
		playlistMenu.add(importJSONMenuItem);
		playlistMenu.add(exportSpotifyMenuItem);
		playlistMenu.add(exportJSONMenuItem);
		
		//Menu Component //Profile Menu
		JMenuItem info = new JMenuItem("User Info");
		info.addActionListener(e -> showUserInfo(man));
		
		JMenuItem playlists = new JMenuItem("User Playlists");
		playlists.addActionListener(e -> showUserPlaylists(playlistContainer,man));
		
		JMenuItem status = new JMenuItem("Authorization Status");
		status.addActionListener(e -> showStatus(man));
		
		
		
		JMenu profile = new JMenu("Profile");
		profile.add(info);
		profile.add(playlists);
		profile.add(status);
		
		
		//MenuBar Composition
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(playlistMenu);
		menuBar.add(profile);
		

		//Application Component
		JFrame frame = new JFrame("Spotify Playlist Manager");

		frame.getContentPane().add(playlistContainerScroll,BorderLayout.CENTER);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500,500);
		frame.setLocationRelativeTo(null);

		frame.setJMenuBar(menuBar);
		frame.setVisible(true);
		
	}
	
	public static void uploadPlaylist(PlaylistManager man){
		String playlistID = new JSONObject(createPlaylist(man)).getString("id");
		JSONArray currentPlaylist = man.playlist;
		int size = currentPlaylist.length();
		int countSize = 0;
		
		
		
		while(countSize <= size){
			JSONArray trackUris = new JSONArray();
			
			for (int i = countSize; i <countSize+100 && i < size; i++){
				trackUris.put(man.playlist.getJSONObject(i).getJSONObject("track").getString("uri"));
				
				
			}
			
			JSONObject trackUriObject = new JSONObject();
			trackUriObject.put("uris",trackUris);
			
			try{
			HttpRequest addSongRequest = HttpRequest.newBuilder()
				.uri(new URI("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks"))
				.headers("Authorization","Bearer " + man.authorizedToken)
				.POST(HttpRequest.BodyPublishers.ofString(trackUriObject.toString()))
				.build();
				
				HttpResponse<String> response = HttpClient.newHttpClient().send(addSongRequest, HttpResponse.BodyHandlers.ofString());
			} catch(Exception e){}
				
			countSize += 100;
		}
		
		
		
	}
	
	public static String createPlaylist(PlaylistManager man){
		
		String responseBody = null;
		JSONObject requestBody = new JSONObject();
		requestBody.put("name","default");
		
		try{
			HttpRequest createRequest = HttpRequest.newBuilder()
				.uri(new URI("https://api.spotify.com/v1/users/" + man.userID + "/playlists"))
				.headers("Authorization","Bearer " + man.authorizedToken)
				.POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
				.build();

			HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, HttpResponse.BodyHandlers.ofString());	
			responseBody = response.body();
		} catch(Exception e){}
		
		
		
		return (responseBody);
	}
	
	public static void showUserPlaylists(JPanel playlistContainer, PlaylistManager man){
		
		
		JSONObject userPlaylistsJSON = new JSONObject(getUserPlaylists(man));
		int userPlaylistsTotal = userPlaylistsJSON.getInt("total");
		JSONArray userPlaylistsJSONArray = userPlaylistsJSON.getJSONArray("items");
		
		
		JPanel userPlaylistsPanel = new JPanel();
		userPlaylistsPanel.setLayout(new BoxLayout(userPlaylistsPanel,BoxLayout.Y_AXIS));
		
		
		
		for(int i = 0; i < userPlaylistsTotal ; i++){
			JSONObject playlistJSON = userPlaylistsJSONArray.getJSONObject(i);
			String playlistID = playlistJSON.getString("id");
			
			JButton loadButton = new JButton("load");
			loadButton.addActionListener(e -> {
				man.playlist = getPlaylistTracks(playlistID);
				populate(playlistContainer, man);
			} );
			
			JLabel playlistName = new JLabel(playlistJSON.getString("name"));
			playlistName.setVerticalAlignment(JLabel.BOTTOM);
			
			JPanel playlistsRow = new JPanel();
			playlistsRow.add(playlistName,JLabel.CENTER);
			playlistsRow.add(loadButton);
			
			userPlaylistsPanel.add(playlistsRow);
			
		}
		
		
		
		
		JFrame userPlaylistsFrame = new JFrame("User Information");
		userPlaylistsFrame.add(userPlaylistsPanel, BorderLayout.CENTER);
		
		userPlaylistsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		userPlaylistsFrame.setLocationRelativeTo(null);
		userPlaylistsFrame.setResizable(false);
		userPlaylistsFrame.pack();
		userPlaylistsFrame.setVisible(true);
	}
	
	public static String getUserPlaylists(PlaylistManager man){
		return (getRequestResponse("https://api.spotify.com/v1/me/playlists",man.authorizedToken));
	}
	
	public static void showUserInfo(PlaylistManager man){
		
		JSONObject userInfoJSON = new JSONObject(getUserProfile(man));
		JLabel profileImageIconLabel = null;
		
		try{
			URL profileImageURL = new URL(userInfoJSON.getJSONArray("images").getJSONObject(0).getString("url"));
			Image profileImage = ImageIO.read(profileImageURL);
			ImageIcon profileImageIcon = new ImageIcon(profileImage.getScaledInstance(100,100,Image.SCALE_DEFAULT));
			profileImageIconLabel = new JLabel(profileImageIcon);
			profileImageIconLabel.setVerticalAlignment(JLabel.TOP);
		} catch (Exception e){}
		
		JLabel name = new JLabel("Name: " + userInfoJSON.getString("display_name"), SwingConstants.CENTER);
		JLabel email = new JLabel("Email: " + userInfoJSON.getString("email"), SwingConstants.CENTER);
		JLabel id = new JLabel("ID: " + userInfoJSON.getString("id"), SwingConstants.CENTER);
		JLabel country = new JLabel("Country: " + userInfoJSON.getString("country"), SwingConstants.CENTER);	
		JLabel subscription = new JLabel("Subscription: " + userInfoJSON.getString("type"), SwingConstants.CENTER);
		
		
		JPanel userInfoPanel = new JPanel();
		userInfoPanel.setLayout(new BoxLayout(userInfoPanel,BoxLayout.Y_AXIS));
		userInfoPanel.add(name);
		userInfoPanel.add(email);
		userInfoPanel.add(id);
		userInfoPanel.add(country);
		userInfoPanel.add(subscription);
		
		JFrame userInfoFrame = new JFrame("User Information");
		userInfoFrame.add(userInfoPanel,BorderLayout.CENTER);
		userInfoFrame.add(profileImageIconLabel, BorderLayout.WEST);
		
		userInfoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		userInfoFrame.setLocationRelativeTo(null);
		userInfoFrame.setResizable(false);
		userInfoFrame.pack();
		userInfoFrame.setVisible(true);
	}
	
	public static String getUserProfile(PlaylistManager man){
		String token = man.authorizedToken;
		return (getRequestResponse("https://api.spotify.com/v1/me",token));
	}
	
	public static void showStatus(PlaylistManager man){
		
		String authorization = man.authorizedToken==null?"Unauthorized":"Authorized";
		
		JFrame prompt = new JFrame("User Status");
		
		JLabel currentStatus = new JLabel("Current Status:" + authorization, SwingConstants.CENTER);
		
		JButton login = new JButton("Login");
		login.addActionListener(e -> {
			man.authorizedToken=getAuthorizedToken();
			if(man.authorizedToken!=null){ 
				currentStatus.setText("Authorized");
				login.setVisible(false);
				man.userID = new JSONObject(getUserProfile(man)).getString("id");
			} 
		});
		
		prompt.getContentPane().add(new JLabel("User Status", SwingConstants.CENTER), BorderLayout.NORTH);
		prompt.getContentPane().add(currentStatus, BorderLayout.CENTER);
		if (authorization.equals("Unauthorized")) prompt.getContentPane().add(login, BorderLayout.SOUTH);
		
		prompt.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		prompt.setLocationRelativeTo(null);
		prompt.setResizable(false);
		prompt.pack();
		prompt.setVisible(true);
	}
	
	
	public static void exportToJSON(PlaylistManager man){
		JFileChooser fc = new JFileChooser();
		int response = fc.showSaveDialog(null);
		
		if(response == JFileChooser.APPROVE_OPTION){
			writeToFile(man.playlist.toString(), new File(fc.getSelectedFile().getAbsolutePath() + ".txt"));
		}
		
	}


	public static void loadPlaylistFromJSON(JPanel playlistContainer, PlaylistManager man){
		JFileChooser fc = new JFileChooser();
		int response = fc.showOpenDialog(null);
		
		if(response == JFileChooser.APPROVE_OPTION){
			man.playlist = new JSONArray(readFromFile(fc.getSelectedFile()));
			populate(playlistContainer,man);
		}
		
	}


	public static void promptPlaylistID(JPanel playlistContainer, PlaylistManager man){
		JFrame prompt = new JFrame("Playlist ID");
		prompt.getContentPane().add(new JLabel("Please Enter Public Playlist ID", SwingConstants.CENTER), BorderLayout.NORTH);

		JTextField playlistInput = new JTextField ();
		playlistInput.addActionListener(e -> {

			playlistInput.disable();		

			if(validatePlaylistID(playlistInput.getText())){
				man.playlist = getPlaylistTracks(playlistInput.getText());
				populate(playlistContainer, man);
				prompt.dispose();
			} else {
				prompt("Incorrect ID", "Incorrect ID");
			}

			playlistInput.enable();
		});

		prompt.getContentPane().add(playlistInput, BorderLayout.CENTER);
		
		prompt.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		prompt.setLocationRelativeTo(null);
		prompt.setResizable(false);

		prompt.pack();
		prompt.setVisible(true);
		
	}

	public static void populate(JPanel playlistContainer, PlaylistManager man){
		

		//progressBar
		JProgressBar proBar = new JProgressBar(0,man.playlist.length());

		JFrame progressWindow = new JFrame("Progress Bar");
		progressWindow.getContentPane().add(proBar, BorderLayout.CENTER);
		
		progressWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		progressWindow.setLocationRelativeTo(null);
		progressWindow.setResizable(false);
		progressWindow.pack();
		progressWindow.setVisible(true);
		

		SwingWorker sw = new SwingWorker(){
			@Override
			protected String doInBackground(){
				for(int i = 0; i<man.playlist.length(); i++) {


					try{
						URL trackCoverUrl = new URL(man.playlist.getJSONObject(i).getJSONObject("track").getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url"));
						Image trackCoverImage = ImageIO.read(trackCoverUrl);
						
						ImageIcon trackCover = new ImageIcon(trackCoverImage.getScaledInstance(100,100,Image.SCALE_DEFAULT));

						JPanel trackBar = new JPanel();
						trackBar.add(new JLabel(Integer.toString(i+1)));
						trackBar.add(new JLabel(trackCover));
					
						playlistContainer.add(trackBar);
					} catch(Exception e){}

					proBar.setValue(i);
					proBar.repaint();
					proBar.revalidate();
					playlistContainer.repaint();
					playlistContainer.revalidate();
				}
				
				progressWindow.dispose();
				
				return ("");
			}
			
			@Override
			protected void done(){
				prompt("Playlist Loader", "Completed Loaded Playlist");
			}
			
		};

		sw.execute();
	}

	public static void prompt(String purpose, String message){
		JFrame prompt = new JFrame(purpose);
		prompt.getContentPane().add(new JLabel(message, SwingConstants.CENTER), BorderLayout.NORTH);
		
		prompt.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		prompt.setLocationRelativeTo(null);
		prompt.setResizable(false);
		prompt.pack();
		prompt.setVisible(true);
	}

	public static boolean validatePlaylistID(String playlistID){
		String response = getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID);
		
		if(response.equals("error")) return false;

		return(true);
	}

	public static String readFromFile(File file){
		String text = null;
		try(Scanner sc = new Scanner(file);){
			text = sc.nextLine();
		} catch(Exception e){}
		return(text);
	}

	public static void writeToFile(String json, File file){
		try(FileWriter fw = new FileWriter(file)){
			fw.write(json);
			fw.close();
		} catch (Exception e){}

	}

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


	public static JSONArray getPlaylistTracks(String playlistID){
		int offSet = 50;
		int limit = 50;
		JSONArray allTracks = new JSONArray();
		String response = getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?limit=50");
		JSONObject responseJSON = new JSONObject (response);
		allTracks.putAll(responseJSON.getJSONArray("items"));
		
		int size = responseJSON.getInt("total");

		while(offSet < size){
			response = getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?limit=50&offset=" + offSet);
			responseJSON = new JSONObject (response);
			allTracks.putAll(responseJSON.getJSONArray("items"));

			offSet += 50;
		}

		return (allTracks);
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
	
	public static String getAuthorizationCode(){
		String site = "https://accounts.spotify.com/authorize?client_id=c592a3c9e9b34be59a79bfe0b98aa6a1&redirect_uri=http://localhost/&response_type=code&scope=user-read-email,user-read-private,playlist-modify-public,playlist-modify-private,playlist-read-private";
		
		WebDriver driver = new ChromeDriver();
		driver.get(site);
		
		new WebDriverWait(driver, Duration.ofMinutes(2)).until(ExpectedConditions.urlContains("localhost/?code"));
		
		String authorizationCode = driver.getCurrentUrl();
		authorizationCode = authorizationCode.substring(authorizationCode.indexOf("=")+1);
		
		driver.quit();
		
		return(authorizationCode);
	}

}

class PlaylistManager{
	JSONArray playlist = null;
	String authorizedToken = null;
	String token = null;
	String userID = null;
}
