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

class PlaylistManager{
	JScrollPane playlistScrollContainer = null;
	JSONArray playlistJSON = null;
	Playlist playlist = null;
	String authorizedToken = null;
	String token = null;
	String userID = null;

	public void moveJSONSong(int from, int to){
		List playlistJSONList = this.playlistJSON.toList();
		
		playlistJSONList.add(to,playlistJSONList.get(from));
		playlistJSONList.remove(from);
		
		this.playlistJSON = new JSONArray(playlistJSONList);
	}
	
	public void showStatus(){
		String authorization = this.authorizedToken==null?"Not Logged in":"Logged in";
		
		JFrame prompt = new JFrame("User Status");
		
		JLabel currentStatus = new JLabel("Current Status:" + authorization, SwingConstants.CENTER);
		
		JButton login = new JButton("Login");
		login.addActionListener(e -> {
			this.authorizedToken=getAuthorizedToken();
			if(this.authorizedToken!=null){ 
				currentStatus.setText("Authorized");
				login.setVisible(false);
				this.userID = new JSONObject(getUserProfile(this)).getString("id");
			} 
		});
		
		prompt.getContentPane().add(new JLabel("User Status", SwingConstants.CENTER), BorderLayout.NORTH);
		prompt.getContentPane().add(currentStatus, BorderLayout.CENTER);
		if (authorization.equals("Not Logged in")) prompt.getContentPane().add(login, BorderLayout.SOUTH);
		
		prompt.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		prompt.setLocationRelativeTo(null);
		prompt.setResizable(false);
		prompt.pack();
		prompt.setVisible(true);
	}
	
	
	
	public static String getUserProfile(PlaylistManager man){
		String token = man.authorizedToken;
		return (getRequestResponse("https://api.spotify.com/v1/me",token));
	}
	
/* 	public static void uploadPlaylist(PlaylistManager man){
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
			HttpRequest addtrackNameRequest = HttpRequest.newBuilder()
				.uri(new URI("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks"))
				.headers("Authorization","Bearer " + man.authorizedToken)
				.POST(HttpRequest.BodyPublishers.ofString(trackUriObject.toString()))
				.build();
				
				HttpResponse<String> response = HttpClient.newHttpClient().send(addtrackNameRequest, HttpResponse.BodyHandlers.ofString());
			} catch(Exception e){}
				
			countSize += 100;
		}
	} */
	
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
	
	public static void showUserPlaylists(PlaylistManager man){
		JSONObject userPlaylistsJSON = new JSONObject(getUserPlaylists(man));
		int userPlaylistsTotal = userPlaylistsJSON.getInt("total");
		JSONArray userPlaylistsJSONArray = userPlaylistsJSON.getJSONArray("items");
		
		JPanel userPlaylistsPanel = new JPanel();
		userPlaylistsPanel.setLayout(new BoxLayout(userPlaylistsPanel,BoxLayout.Y_AXIS));
		
		JPanel playlistsRowHeader = new JPanel();
		playlistsRowHeader.add(new JLabel("Playlists"));
		userPlaylistsPanel.add(playlistsRowHeader);
		
		
		for(int i = 0; i < userPlaylistsTotal ; i++){
			JSONObject playlistJSON = userPlaylistsJSONArray.getJSONObject(i);
			String playlistID = playlistJSON.getString("id");
			
			JButton loadButton = new JButton("load");
			loadButton.addActionListener(e -> {
				man.playlistJSON = generateCustomJSON(playlistID);
				populate(man);
			} );
			
			JLabel playlistName = new JLabel(playlistJSON.getString("name"));
			playlistName.setVerticalAlignment(JLabel.BOTTOM);
			
			JPanel playlistsRow = new JPanel();
			playlistsRow.add(playlistName,JLabel.CENTER);
			playlistsRow.add(loadButton);
			playlistsRow.setPreferredSize(new Dimension(500,50));
			
			userPlaylistsPanel.add(playlistsRow);
		}
		
		JFrame userPlaylistsFrame = new JFrame("User Playlists");
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
	
	
	public static void exportToJSON(PlaylistManager man){
		JFileChooser fc = new JFileChooser();
		int response = fc.showSaveDialog(null);
		
		if(response == JFileChooser.APPROVE_OPTION){
			writeToFile(man.playlistJSON.toString(), new File(fc.getSelectedFile().getAbsolutePath() + ".txt"));
		}
	}


	public static void loadPlaylistFromJSON(PlaylistManager man){
		JFileChooser fc = new JFileChooser();
		int response = fc.showOpenDialog(null);
		
		if(response == JFileChooser.APPROVE_OPTION){
			man.playlistJSON = new JSONArray(readFromFile(fc.getSelectedFile()));
			populate(man);
		}
	}


	public static void promptPlaylistID(PlaylistManager man){
		JFrame prompt = new JFrame("Playlist ID");
		prompt.getContentPane().add(new JLabel("Please Enter Public Playlist ID", SwingConstants.CENTER), BorderLayout.NORTH);

		JTextField playlistInput = new JTextField ();
		playlistInput.addActionListener(e -> {

			playlistInput.disable();		

			if(validatePlaylistID(playlistInput.getText())){
				man.playlistJSON = generateCustomJSON(playlistInput.getText());
				populate(man);
				prompt.dispose();
			} else {
				JOptionPane.showMessageDialog(null, "Invalid Playlist ID", "Error", JOptionPane.ERROR_MESSAGE);
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

	public static void populate(PlaylistManager man){
		int isReplace = 0;
		if(man.playlist.getComponents().length!=0) {
			isReplace = JOptionPane.showConfirmDialog(null,"There is a playlist loaded. Replace it?","Playlist Replacement",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
		}
		
		if(isReplace == JOptionPane.YES_OPTION){
			
			man.playlist = new Playlist(man);
			man.playlistScrollContainer.setViewportView(man.playlist);
			man.playlistScrollContainer.repaint();
			man.playlistScrollContainer.revalidate();
			
			
			//progressBar
			JProgressBar proBar = new JProgressBar(0,man.playlistJSON.length());

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
					
					GridBagConstraints c = new GridBagConstraints();
					
					//Table Header - Visible Labels
					Track header = new Track(0, man.playlist,true,null);
					
					c.gridx = 0;
					c.insets = new Insets(0,3,0,3);
					Data numHeaderLabel = new Data ("#", true,true,c, header);
					numHeaderLabel.setPreferredSize(new Dimension(25,25));
					header.num = numHeaderLabel;
					header.add(numHeaderLabel,c);
					
					c = new GridBagConstraints();
					c.gridx = 1;
					c.insets = new Insets(0,3,0,3);
					Data coverHeaderLabel = new Data("Cover",true,true,c, header);
					coverHeaderLabel.setPreferredSize(new Dimension(100,100));
					header.cover = coverHeaderLabel;
					header.add(coverHeaderLabel,c);
					
					c = new GridBagConstraints();
					c.gridx = 2;
					c.insets = new Insets(0,3,0,3);
					Data trackNameHeaderLabel = new Data("Track",true,true,c, header);
					trackNameHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.name = trackNameHeaderLabel;
					header.add(trackNameHeaderLabel,c);
					
					c = new GridBagConstraints();
					c.gridx = 3;
					c.insets = new Insets(0,3,0,3);
					Data artistNameHeaderLabel = new Data("Artists",true,true,c, header);
					artistNameHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.artist = artistNameHeaderLabel;
					header.add(artistNameHeaderLabel,c);
					
					
					//Table Header - Invisible Labels
					c = new GridBagConstraints();
					c.gridx = 4;
					c.insets = new Insets(0,3,0,3);
					Data releasedDateHeaderLabel = new Data("Released Date",true,false,c, header);
					releasedDateHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.releasedDate = releasedDateHeaderLabel;
					
					c = new GridBagConstraints();
					c.gridx = 5;
					c.insets = new Insets(0,3,0,3);
					Data durationHeaderLabel = new Data("Duration",true,false,c, header);
					durationHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.duration = durationHeaderLabel;
					
					c = new GridBagConstraints();
					c.gridx = 6;
					c.insets = new Insets(0,3,0,3);
					Data popularityLabelHeaderLabel = new Data("Popularity",true,false,c, header);
					popularityLabelHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.popularity = popularityLabelHeaderLabel;
					
					c = new GridBagConstraints();
					c.gridx = 7;
					c.insets = new Insets(0,3,0,3);
					Data explicitLabelHeaderLabel = new Data("Explicit",true,false,c, header);
					explicitLabelHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.explicit = explicitLabelHeaderLabel;
					
					c = new GridBagConstraints();
					c.gridx = 8;
					c.insets = new Insets(0,3,0,3);
					Data artistTypeLabelHeaderLabel = new Data("Artist Type",true,false,c, header);
					artistTypeLabelHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.artistType = artistTypeLabelHeaderLabel;
					
					c = new GridBagConstraints();
					c.gridx = 9;
					c.insets = new Insets(0,3,0,3);
					Data artistCountryLabelHeaderLabel = new Data("Artist Country",true,false, c, header);
					artistCountryLabelHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.artistCountry = artistCountryLabelHeaderLabel;
					
					c = new GridBagConstraints();
					c.gridx = 10;
					c.insets = new Insets(0,3,0,3);
					Data artistGenderLabelHeaderLabel = new Data("Gender",true,false,c, header);
					artistGenderLabelHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.artistGender = artistGenderLabelHeaderLabel;
					
					c = new GridBagConstraints();
					c.gridx = 11;
					c.insets = new Insets(0,3,0,3);
					Data isDeadLabelHeaderLabel = new Data("Deceased",true,false,c, header);
					isDeadLabelHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.isDead = isDeadLabelHeaderLabel;
					
					c = new GridBagConstraints();
					c.gridx = 12;
					c.insets = new Insets(0,3,0,3);
					Data subAreaLabelHeaderLabel = new Data("Sub-Location",true,false,c, header);
					subAreaLabelHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.subArea = subAreaLabelHeaderLabel;
					
					c = new GridBagConstraints();
					c.gridx = 13;
					c.insets = new Insets(0,3,0,3);
					Data languageLabelHeaderLabel = new Data("Language",true,false,c, header);
					languageLabelHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.language = languageLabelHeaderLabel;
					
					c = new GridBagConstraints();
					c.gridy = 0;
					c.weightx = 1;
					header.constraints = c;
					man.playlist.add(header,c);
			
					
					//Table Body
					for(int i = 0; i<man.playlistJSON.length(); i++) {
						JSONObject currentTrack = man.playlistJSON.getJSONObject(i);
						Track newTrack = new Track(i+1,man.playlist,false,currentTrack);

						try{
							URL trackCoverUrl = new URL(currentTrack.getString("trackCoverURL"));
							Image trackCoverImage = ImageIO.read(trackCoverUrl);
							ImageIcon trackCover = new ImageIcon(trackCoverImage.getScaledInstance(100,100,Image.SCALE_DEFAULT));
							
							c = new GridBagConstraints();
							c.gridx = 0;
							c.insets = new Insets(3,3,3,3);
							Data numLabel = new Data (Integer.toString(i+1), false,true, c, newTrack);
							numLabel.setPreferredSize(new Dimension(25,25));
							newTrack.num = numLabel;
							
							c = new GridBagConstraints();
							c.gridx = 1;
							c.insets = new Insets(3,3,3,3);
							Data coverLabel = new Data(trackCover, false,true, c, newTrack);
							coverLabel.setPreferredSize(new Dimension(100,100));
							newTrack.cover = coverLabel;
							
							c = new GridBagConstraints();
							c.gridx = 2;
							c.insets = new Insets(3,3,3,3);
							Data trackNameLabel = new Data(currentTrack.getString("trackName"),false,true,c, newTrack);
							trackNameLabel.setPreferredSize(new Dimension(150,150));
							newTrack.name = trackNameLabel;
							
							c = new GridBagConstraints();
							c.gridx = 3;
							c.insets = new Insets(3,3,3,3);
							Data artistNameLabel = new Data(currentTrack.getString("trackArtist"),false,true,c, newTrack);
							artistNameLabel.setPreferredSize(new Dimension(150,150));
							newTrack.artist = artistNameLabel;
							
							//Hidden
							c = new GridBagConstraints();
							c.gridx = 4;
							c.insets = new Insets(3,3,3,3);
							Data releasedDateLabel = new Data(currentTrack.getString("releasedDate"),false,false,c, newTrack);
							releasedDateLabel.setPreferredSize(new Dimension(150,150));
							newTrack.releasedDate = releasedDateLabel;
							
							c = new GridBagConstraints();
							c.gridx = 5;
							c.insets = new Insets(3,3,3,3);
							Data durationLabel = new Data(currentTrack.getString("duration"),false,false,c, newTrack);
							durationLabel.setPreferredSize(new Dimension(150,150));
							newTrack.duration = durationLabel;
							
							c = new GridBagConstraints();
							c.gridx = 6;
							c.insets = new Insets(3,3,3,3);
							Data popularityLabel = new Data(currentTrack.getString("popularity"),false,false,c, newTrack);
							popularityLabel.setPreferredSize(new Dimension(150,150));
							newTrack.popularity = popularityLabel;
							
							c = new GridBagConstraints();
							c.gridx = 7;
							c.insets = new Insets(3,3,3,3);
							Data explicitLabel = new Data(currentTrack.getString("explicit"),false,false,c, newTrack);
							explicitLabel.setPreferredSize(new Dimension(150,150));
							newTrack.explicit = explicitLabel;
							
							c = new GridBagConstraints();
							c.gridx = 8;
							c.insets = new Insets(3,3,3,3);
							Data artistTypeLabel = new Data(currentTrack.getString("artistType"),false,false,c, newTrack);
							artistTypeLabel.setPreferredSize(new Dimension(150,150));
							newTrack.artistType = artistTypeLabel;
							
							c = new GridBagConstraints();
							c.gridx = 9;
							c.insets = new Insets(3,3,3,3);
							Data artistCountryLabel = new Data(currentTrack.getString("artistCountry"),false,false,c, newTrack);
							artistCountryLabel.setPreferredSize(new Dimension(150,150));
							newTrack.artistCountry = artistCountryLabel;
							
							c = new GridBagConstraints();
							c.gridx = 10;
							c.insets = new Insets(3,3,3,3);;
							Data artistGenderLabel = new Data(currentTrack.getString("artistGender"),false,false,c, newTrack);
							artistGenderLabel.setPreferredSize(new Dimension(150,150));
							newTrack.artistGender = artistGenderLabel;
							
							c = new GridBagConstraints();
							c.gridx = 11;
							c.insets = new Insets(3,3,3,3);
							Data isDeadLabel = new Data(currentTrack.getString("isDead"),false,false,c, newTrack);
							isDeadLabel.setPreferredSize(new Dimension(150,150));
							newTrack.isDead = isDeadLabel;
							
							c = new GridBagConstraints();
							c.gridx = 12;
							c.insets = new Insets(3,3,3,3);
							Data subAreaLabel = new Data(currentTrack.getString("subArea"),false,false,c, newTrack);
							subAreaLabel.setPreferredSize(new Dimension(150,150));
							newTrack.subArea = subAreaLabel;
							
							c = new GridBagConstraints();
							c.gridx = 13;
							c.insets = new Insets(3,3,3,3);
							Data languageLabel = new Data(currentTrack.getString("language"),false,false,c, newTrack);
							languageLabel.setPreferredSize(new Dimension(150,150));
							newTrack.language = languageLabel;
							
							newTrack.setBorder(BorderFactory.createLineBorder(Color.black));
							
							c = new GridBagConstraints();
							c.gridy = i+1;
							c.gridx = 0;
							c.weightx = 1;
							newTrack.constraints = c;
							man.playlist.add(newTrack,c);
							
							newTrack.initializeTrack();
						
						} catch(Exception e){
							System.out.println(e.getMessage());
							e.printStackTrace();
						}
						
						proBar.setValue(i);
						proBar.repaint();
						proBar.revalidate();
						man.playlist.repaint();
						man.playlist.revalidate();
					}
					
					progressWindow.dispose();
		
					return ("");
				}
			
				@Override
				protected void done(){
					//prompt("Playlist Loader", "Completed Loaded Playlist");
				}
			};

			sw.execute();
		}
	}

	public static boolean validatePlaylistID(String playlistID){
		String response = getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID);
		JSONObject responseJSON = new JSONObject(response);
		
		if(responseJSON.optString("error")!=null) return false;

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
	
	public static JSONArray generateCustomJSON(String playlistID){
		
		JSONArray spotifyPlaylist = getPlaylistTracks(playlistID);
		
		JSONArray customArray = new JSONArray();
		int counter = 0;
		
		for(int i = 0; i < spotifyPlaylist.length(); i++){
			JSONObject currentTrack = spotifyPlaylist.getJSONObject(i).getJSONObject("track");
			JSONObject newJSON = parseTrackJSON(currentTrack);
			
			customArray.put(counter++,newJSON);
		}
		
		loadMusicBrainz(customArray);
		
		return(customArray);
	}
	
	public static JSONObject parseTrackJSON(JSONObject currentTrack){
		JSONObject newJSON = new JSONObject();
		//resolve voided tracks
		if (!(currentTrack.getJSONObject("album").getString("release_date").equals("0000"))){
			String trackCoverURL = currentTrack.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
			newJSON.put("trackCoverURL", trackCoverURL);
			
			String trackName = currentTrack.getString("name");
			newJSON.put("trackName",trackName);
			
			String trackArtist = currentTrack.getJSONArray("artists").getJSONObject(0).getString("name");
			newJSON.put("trackArtist", trackArtist);
			
			String album = currentTrack.getJSONObject("album").getString("name");
			newJSON.put("album", album);
			
			String releasedDate = currentTrack.getJSONObject("album").getString("release_date");
			newJSON.put("releasedDate",releasedDate);
			
			int duration = currentTrack.getInt("duration_ms");
			int min = (duration/1000/60);
			int sec = (duration/1000%60);
			String durationString = String.format("%02d:%02d",min,sec);
			newJSON.put("duration",durationString);
			
			int popularity = currentTrack.getInt("popularity");
			newJSON.put("popularity",String.valueOf(popularity));
			
			String artistID = currentTrack.getJSONArray("artists").getJSONObject(0).getString("id");
			newJSON.put("artistID",artistID);
			
			String explicit = String.valueOf(currentTrack.getBoolean("explicit")); 
			newJSON.put("explicit",explicit);
			
			String isrc = currentTrack.getJSONObject("external_ids").getString("isrc");
			newJSON.put("isrc",isrc);
			
			String id = currentTrack.getString("id");
			newJSON.put("id",id);
		}
		
		return (newJSON);
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