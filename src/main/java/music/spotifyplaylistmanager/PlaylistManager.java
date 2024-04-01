package music.spotifyplaylistmanager;

import java.net.URL;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.awt.BorderLayout;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import org.json.JSONObject;
import org.json.JSONArray;
import java.time.Duration;
import java.awt.Color;
import javax.swing.BorderFactory;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.JTextArea;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.event.ChangeListener;
import javax.swing.SwingUtilities;

class PlaylistManager{
	JScrollPane playlistScrollContainer = null;
	JFrame mainFrame;
	Recommender recom;
	Help help;
	MusicPlayer mp;
	JSONArray playlistJSON = null;
	Playlist playlist = null;
	String authorizedToken = null;
	String token = null;
	String userID = null;

	public void moveJSONSong(int from, int to){
		
		ArrayList<JSONObject> JSONTracks = new ArrayList();
		Iterator it = this.playlistJSON.iterator();
		
		while(it.hasNext()){
			JSONTracks.add(new JSONObject(it.next().toString()));
		}
		
		JSONObject removed = JSONTracks.get(from);
		JSONTracks.remove(from);
		JSONTracks.add(to,removed);
		
		this.playlistJSON = new JSONArray(JSONTracks);
	}
	
	public void showStatus(){
		String authorization = this.authorizedToken==null?"Not Logged in":"Logged in";
		
		JFrame prompt = new JFrame("User Status");
		
		JLabel currentStatus = new JLabel("Current Status:" + authorization, SwingConstants.CENTER);
		
		JButton login = new JButton("Login");
		login.addActionListener(e -> {
			this.authorizedToken = APIHandler.getAuthorizedToken();
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
		return (APIHandler.getRequestResponse("https://api.spotify.com/v1/me",token));
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
		return (APIHandler.getRequestResponse("https://api.spotify.com/v1/me/playlists",man.authorizedToken));
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
	
	public static boolean validatePlaylistID(String playlistID){
		if(playlistID.equals("")) return(false);
		
		String response = APIHandler.getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID);
		JSONObject responseJSON = new JSONObject(response);
		
		if(responseJSON.optJSONObject("error")!=null)return(false);
		
		return(true);
	}


	public static void promptPlaylistID(PlaylistManager man){
		JFrame prompt = new JFrame("Playlist ID");
		prompt.getContentPane().add(new JLabel("Please Enter Public Playlist ID", SwingConstants.CENTER), BorderLayout.NORTH);

		JTextField playlistInput = new JTextField ();
		playlistInput.addActionListener(e -> {
			
			SwingWorker sw = new SwingWorker(){
				@Override
				protected String doInBackground(){
					playlistInput.disable();	
					if(validatePlaylistID(playlistInput.getText())){
						man.playlistJSON = generateCustomJSON(playlistInput.getText());
						populate(man);
						prompt.dispose();
					} else {
						JOptionPane.showMessageDialog(null, "Invalid Playlist ID", "Error", JOptionPane.ERROR_MESSAGE);
					}
					
					return("1");
				}
				
				@Override
				protected void done(){
					playlistInput.enable();
				}
				
			};
			
			sw.execute();
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
			ProgressBarDialog pbd = new ProgressBarDialog("Loading Tracks", new JProgressBar(0,man.playlistJSON.length()));
			
			SwingWorker sw = new SwingWorker(){
				@Override
				protected String doInBackground(){
					
					GridBagConstraints c = new GridBagConstraints();
					
					//Table Header - Visible Labels
					Track header = new Track(man.playlist,true,null);
					c.weightx = 1;
					c.gridx = 0;
					c.insets = new Insets(0,3,0,3);
					Data numHeaderLabel = new Data ("#", true,true,c, header);
					numHeaderLabel.setPreferredSize(new Dimension(25,25));
					header.num = numHeaderLabel;
					header.add(numHeaderLabel,c);
					
					c = new GridBagConstraints();
					c.gridx = 1;
					c.weightx = 1;
					c.insets = new Insets(0,3,0,3);
					Data coverHeaderLabel = new Data("Cover",true,true,c, header);
					coverHeaderLabel.setPreferredSize(new Dimension(100,100));
					header.cover = coverHeaderLabel;
					header.add(coverHeaderLabel,c);
					
					c = new GridBagConstraints();
					c.gridx = 2;
					c.weightx = 1;
					c.insets = new Insets(0,3,0,3);
					Data trackNameHeaderLabel = new Data("Track",true,true,c, header);
					trackNameHeaderLabel.setPreferredSize(new Dimension(150,150));
					header.name = trackNameHeaderLabel;
					header.add(trackNameHeaderLabel,c);
					
					c = new GridBagConstraints();
					c.gridx = 3;
					c.weightx = 1;
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
					c.fill = GridBagConstraints.HORIZONTAL;
					header.constraints = c;
					man.playlist.add(header,c);
			
					
					//Table Body
					for(int i = 0; i<man.playlistJSON.length(); i++) {
						JSONObject currentTrack = man.playlistJSON.getJSONObject(i);
						Track newTrack = new Track(man.playlist,false,currentTrack);

						try{
							URL trackCoverUrl = new URL(currentTrack.getString("trackCoverURL"));
							BufferedImage trackCoverImage = ImageIO.read(trackCoverUrl);
							newTrack.setPalette(trackCoverImage);
							
							ImageIcon trackCover = new ImageIcon(trackCoverImage.getScaledInstance(100,100,Image.SCALE_DEFAULT));
							
							c = new GridBagConstraints();
							c.gridx = 0;
							c.weightx = 1;
							c.insets = new Insets(3,3,3,3);
							Data numLabel = new Data (Integer.toString(i+1), false,true, c, newTrack);
							numLabel.setPreferredSize(new Dimension(25,25));
							newTrack.num = numLabel;
							
							c = new GridBagConstraints();
							c.gridx = 1;
							c.weightx = 1;
							c.insets = new Insets(3,3,3,3);
							Data coverLabel = new Data(trackCover, false,true, c, newTrack);
							coverLabel.setPreferredSize(new Dimension(100,100));
							newTrack.cover = coverLabel;
							
							c = new GridBagConstraints();
							c.gridx = 2;
							c.weightx = 1;
							c.insets = new Insets(3,3,3,3);
							Data trackNameLabel = new Data(currentTrack.getString("trackName"),false,true,c, newTrack);
							trackNameLabel.setPreferredSize(new Dimension(150,150));
							newTrack.name = trackNameLabel;
							
							c = new GridBagConstraints();
							c.gridx = 3;
							c.weightx = 1;
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
							
						
							
							c = new GridBagConstraints();
							c.gridy = i+1;
							c.fill = GridBagConstraints.HORIZONTAL;
							c.gridx = 0;
							c.weightx = 1;
							newTrack.constraints = c;
							newTrack.setBorder(BorderFactory.createLineBorder(Color.BLACK));
							man.playlist.add(newTrack,c);
							
							newTrack.initializeTrack();
						
						} catch(Exception e){
							System.out.println(e.getMessage());
							e.printStackTrace();
						}
						
						
						pbd.incrementValue();
						man.playlist.repaint();
						man.playlist.revalidate();
					}
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

	public static JSONArray getPlaylistTracks(String playlistID){
		int offSet = 0;
		JSONArray allTracks = new JSONArray();
		String response = APIHandler.getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?limit=50");
		JSONObject responseJSON = new JSONObject (response);
		int size = responseJSON.getInt("total");
		
		
		ProgressBarDialog pbd = new ProgressBarDialog("Loading Songs From Spotify", new JProgressBar(0,size));
		
		while(offSet < size){
			response = APIHandler.getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?limit=50&offset=" + offSet);
			responseJSON = new JSONObject (response);
			allTracks.putAll(responseJSON.getJSONArray("items"));

			offSet += 50;
			pbd.setValue(offSet);
		}
		
		return (allTracks);
	}
	
	public static JSONArray generateCustomJSON(String playlistID){
		JSONArray spotifyPlaylist = getPlaylistTracks(playlistID);
		
		JSONArray customArray = new JSONArray();
		int counter = 0;
		
		ProgressBarDialog pbd = new ProgressBarDialog("Reading Data From Spotify", new JProgressBar(0,spotifyPlaylist.length()));
		
		for(int i = 0; i < spotifyPlaylist.length(); i++){
			JSONObject currentTrack = spotifyPlaylist.getJSONObject(i).getJSONObject("track");
			JSONObject newJSON = parseTrackJSON(currentTrack);
			
			customArray.put(counter++,newJSON);
			pbd.incrementValue();
		}
		APIHandler.loadMusicBrainz(customArray);
		
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
			
			int durationInMs = currentTrack.getInt("duration_ms");
			int durationInSeconds = (durationInMs/1000);
			String durationInSecondsString = String.valueOf(durationInSeconds);
			newJSON.put("durationInSeconds",durationInSecondsString);
			
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
}

class ProgressBarDialog extends JDialog{
	JProgressBar pb;
	
	public ProgressBarDialog(String progress, JProgressBar pb){
		super(new JFrame(), progress,false);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(250,50);
		this.setLocationRelativeTo(null);
		this.pb = pb;
		pb.setStringPainted(true);
		pb.setString("0%");
		
		
		pb.addChangeListener(e -> {
			if(pb.getValue() >= pb.getMaximum()){
				ProgressBarDialog.this.dispose();
			}
		});
		this.add(pb,BorderLayout.CENTER);
		this.setVisible(true);
	}
	
	public void incrementValue(){
		pb.setValue(pb.getValue()+1);
		double percentage = (((double)pb.getValue())/pb.getMaximum())*100;
		pb.setString(String.format("%2.0f%%",percentage));
		this.repaint();
		this.revalidate();
	}
	
	public void setValue(int val){
		pb.setValue(val);
		double percentage = (((double)pb.getValue())/pb.getMaximum())*100;
		pb.setString(String.format("%2.0f%%",percentage));
		this.repaint();
		this.revalidate();
	}
}

class Help extends JDialog{
	public Help(PlaylistManager man){
		super(man.mainFrame,"Help",true);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(500,500);
		this.setLocationRelativeTo(null);
		man.help = this;
		
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosed(WindowEvent e){
				man.help = null;
			}
			
			@Override
			public void windowDeactivated(WindowEvent e){
			}
		});
		
		JTextArea helpInstructions = new JTextArea();
		helpInstructions.setLineWrap(true);
		helpInstructions.setWrapStyleWord(true);
		String movingTracks = "If tracks are not moving, be sure that no column is being ordered as track movement is disabled when sorted by any column.  To disable, click on sorted column until no sort symbol is shown or click on the number column to reset order.";
		String playingMusic = "If you have trouble playing music, make sure you have yt-dlp downloaded and have added it to your system path as this application uses it via commandline.";
		String others = "If you encounter any other problems, please contact me at ngawang30@gmail.com with issues.";
		
		JList helpOptions = new JList(new String[]{"Moving Tracks","Playing Music","Others"});
		helpOptions.addListSelectionListener(e -> {
			if(helpOptions.getSelectedValue().equals("Moving Tracks")){
				helpInstructions.setText(movingTracks);
			}
			
			if(helpOptions.getSelectedValue().equals("Playing Music")){
				helpInstructions.setText(playingMusic);
			}
			
			if(helpOptions.getSelectedValue().equals("Others")){
				helpInstructions.setText(others);
			}
		});
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,helpOptions,helpInstructions);
		split.setResizeWeight(.2);
		
		this.add(split,BorderLayout.CENTER);
		
		this.setVisible(true);
	}
}