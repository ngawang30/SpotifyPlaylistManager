package music.spotifyplaylistmanager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.net.URL;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.awt.BorderLayout;
import java.awt.Image;
import javax.imageio.ImageIO;
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
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import java.util.ArrayList;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.JTextArea;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Base64;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

class PlaylistManager {

    private JFrame frame;
    private JScrollPane playlistScrollContainer = null;
    private Track header;
    private MusicPlayer musicPlayer;
    private JSONArray playlistJSON = null;
    private Playlist playlist = null;
    private String userID = null;
    private static String authorizedToken = null;

    public JFrame getFrame() {
        return frame;
    }
    
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }
    
    public JScrollPane getPlaylistScrollContainer() {
        return playlistScrollContainer;
    }

    public void setPlaylistScrollContainer(JScrollPane playlistScrollContainer) {
        this.playlistScrollContainer = playlistScrollContainer;
    }

    public Track getHeader() {
        return header;
    }

    public void setHeader(Track header) {
        this.header = header;
    }

    public MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }

    public void setMusicPlayer(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    public JSONArray getPlaylistJSON() {
        return playlistJSON;
    }

    public void setPlaylistJSON(JSONArray playlistJSON) {
        this.playlistJSON = playlistJSON;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public static String getAuthorizedToken() {
        return authorizedToken;
    }

    public static void setAuthorizedToken(String authorizedToken) {
        PlaylistManager.authorizedToken = authorizedToken;
    }
    
    
    public void app() {
        FlatDarkLaf.setup();

        frame = new JFrame("Spotify Playlist Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);
        frame.setLocationRelativeTo(null);

//        try {
//            
//            ArrayList<Image> images = new ArrayList();
//            
//            images.add(new ImageIcon("./res/img/SPMLogox40.png").getImage());
//            images.add(new ImageIcon("./res/img/SPMLogox30.png").getImage());
//            images.add(new ImageIcon("./res/img/SPMLogox20.png").getImage());
//               
//            frame.setIconImages(images);
//
//        } catch (Exception e){
//            e.printStackTrace();
//        }

        PlaylistManager man = new PlaylistManager();
        man.setPlaylist(new Playlist(man));
        man.setMusicPlayer(new MusicPlayer());
        man.setFrame(frame);
        frame.add(man.getMusicPlayer(), BorderLayout.SOUTH);
        
        
        JScrollPane playlistContainerScroll = new JScrollPane();
        playlistContainerScroll.setViewportView(man.getPlaylist());
        playlistContainerScroll.getVerticalScrollBar().setUnitIncrement(10);
        frame.add(playlistContainerScroll, BorderLayout.CENTER);

        man.setPlaylistScrollContainer(playlistContainerScroll);

        //Menu Component //File Item
        JMenuItem importSpotifyMenuItem = new JMenuItem("Import Playlist From Spotify");
        importSpotifyMenuItem.addActionListener(e -> {
            if (man.isAuthorizedNotify()) {
                man.promptPlaylistID();
            }
        });

        JMenuItem importJSONMenuItem = new JMenuItem("Import Playlist From JSON");
        importJSONMenuItem.addActionListener(e -> man.loadPlaylistFromJSON());

        JMenuItem exportJSONMenuItem = new JMenuItem("Export Playlist to JSON");
        exportJSONMenuItem.addActionListener(e -> {
            if (!man.getPlaylist().isEmptyNotify()) {
                man.exportToJSON();
            }
        });

        JMenuItem exportSpotifyMenuItem = new JMenuItem("Export Playlist to Spotify");
        exportSpotifyMenuItem.addActionListener(e -> {
            if (man.isAuthorizedNotify() && !man.getPlaylist().isEmptyNotify()) {
                //uploadPlaylist(man);
            }
        });

        JMenuItem viewJSON = new JMenuItem("View JSON");
        viewJSON.addActionListener(e -> {
            JDialog jsonViewer = new JDialog(frame);
            jsonViewer.setSize(400, 400);
            jsonViewer.setLocationRelativeTo(frame);

            JTextArea text = new JTextArea();
            text.setEditable(false);
            text.setLineWrap(true);
            text.setWrapStyleWord(true);

            if (man.getPlaylistJSON() != null) {
                Iterator it = man.getPlaylistJSON().iterator();
                while (it.hasNext()) {
                    text.append(it.next().toString() + "\n\n");
                }
                text.setCaretPosition(0);
            }

            JScrollPane scroll = new JScrollPane(text);
            JScrollBar scrollBar = scroll.getVerticalScrollBar();

            jsonViewer.add(scroll, BorderLayout.CENTER);
            jsonViewer.setVisible(true);
        });

        JMenuItem clearCacheItem = new JMenuItem("Clear Cache");
        clearCacheItem.addActionListener(e -> PlaylistManager.clearCache());

        JMenu FileMenu = new JMenu("File");

        FileMenu.add(importSpotifyMenuItem);
        FileMenu.add(importJSONMenuItem);
        FileMenu.add(exportSpotifyMenuItem);
        FileMenu.add(exportJSONMenuItem);
        FileMenu.addSeparator();
        FileMenu.add(viewJSON);
        FileMenu.add(clearCacheItem);

        //Menu Component //Profile Menu
        JMenuItem info = new JMenuItem("User Info");
        info.addActionListener(e -> {
            if (man.isAuthorizedNotify()) {
                man.showUserInfo();
            }
        });

        JMenuItem playlists = new JMenuItem("User Playlists");
        playlists.addActionListener(e -> {
            if (man.isAuthorizedNotify()) {
                man.showUserPlaylists();
            }
        });

        JMenuItem status = new JMenuItem("Login Status");
        status.addActionListener(e -> {
            man.showStatus();
        });

        JMenu profile = new JMenu("Profile");
        profile.add(info);
        profile.add(playlists);
        profile.add(status);

        //Menu Component //Recommendation
        JMenuItem recommendation = new JMenuItem("Recommendation");
        recommendation.addActionListener(e -> {
            if (man.isAuthorizedNotify()) {
                new Recommender(man);
            }
        });

        JMenuItem playlistTournament = new JMenuItem("Tournament");
        playlistTournament.addActionListener(e -> {
            if (!man.getPlaylist().isEmptyNotify()) {
                new PlaylistComparator(man);
            }
        });

        JMenu functions = new JMenu("Functions");
        functions.add(playlistTournament);
        functions.add(recommendation);

        //Menu Component //Help
        JMenuItem openHelp = new JMenuItem("Open");
        openHelp.addActionListener(e -> new Help(man));

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(openHelp);

        //LightMode
        JMenuItem toggleLightMode = new JMenuItem("🔆");
        toggleLightMode.addActionListener(e -> {
            Window[] windows = man.frame.getOwnedWindows();
            String lookAndFeel = UIManager.getLookAndFeel().getName();
            if (lookAndFeel.equals("FlatLaf Dark")) {
                FlatLightLaf.setup();
            } else {
                FlatDarkLaf.setup();  
            }
            
                SwingUtilities.updateComponentTreeUI(frame);
                
                for(Window win: windows) SwingUtilities.updateComponentTreeUI(win);

        });

        //MenuBar Composition
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(toggleLightMode);
        menuBar.add(FileMenu);
        menuBar.add(profile);
        menuBar.add(functions);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);

        frame.setVisible(true);
    }

    
    
    
    
    public void swapJSONTracks(int from, int to) {
        ArrayList<JSONObject> JSONTracks = this.playlist.getTracksJSONArrayList();

        Collections.swap(JSONTracks, from, to);

        this.playlistJSON = new JSONArray(JSONTracks);
    }

    public void insertJSONTrack(int from, int to) {

        ArrayList<JSONObject> JSONTracks = this.playlist.getTracksJSONArrayList();

        JSONObject removed = JSONTracks.get(from);
        JSONTracks.remove(from);
        JSONTracks.add(to, removed);

        this.playlistJSON = new JSONArray(JSONTracks);
    }

    public void showStatus() {
        String authorization = PlaylistManager.authorizedToken == null ? "Not Logged in" : "Logged in";

        JDialog prompt = new JDialog(this.getFrame(), "User Status");

        JLabel currentStatus = new JLabel("Current Status:" + authorization, SwingConstants.CENTER);

        JButton login = new JButton("Login");
        login.addActionListener(e -> {
            PlaylistManager.authorizedToken = APIHandler.getAuthorizedToken();
            if (PlaylistManager.authorizedToken != null) {
                currentStatus.setText("Authorized");
                login.setVisible(false);
                PlaylistManager.this.userID = new JSONObject(this.getUserProfile()).getString("id");
            }
        });

        prompt.getContentPane().add(new JLabel("User Status", SwingConstants.CENTER), BorderLayout.NORTH);
        prompt.getContentPane().add(currentStatus, BorderLayout.CENTER);
        if (authorization.equals("Not Logged in")) {
            prompt.getContentPane().add(login, BorderLayout.SOUTH);
        }

        prompt.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        prompt.setLocationRelativeTo(null);
        prompt.setResizable(false);
        prompt.pack();
        prompt.setVisible(true);
    }

    public String getUserProfile() {
        return (APIHandler.getRequestResponse("https://api.spotify.com/v1/me"));
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
    public static String createPlaylist(PlaylistManager man) {
        String responseBody = null;
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "default");

        try {
            HttpRequest createRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://api.spotify.com/v1/users/" + man.userID + "/playlists"))
                    .headers("Authorization", "Bearer " + man.authorizedToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(createRequest, HttpResponse.BodyHandlers.ofString());
            responseBody = response.body();
        } catch (Exception e) {
        }

        return (responseBody);
    }

    public void showUserPlaylists() {
        JSONObject userPlaylistsJSON = new JSONObject(this.getUserPlaylists());
        int userPlaylistsTotal = userPlaylistsJSON.getInt("total");
        JSONArray userPlaylistsJSONArray = userPlaylistsJSON.getJSONArray("items");

        JPanel userPlaylistsPanel = new JPanel();
        userPlaylistsPanel.setLayout(new BoxLayout(userPlaylistsPanel, BoxLayout.Y_AXIS));

        JPanel playlistsRowHeader = new JPanel();
        playlistsRowHeader.add(new JLabel("Playlists"));
        userPlaylistsPanel.add(playlistsRowHeader);

        for (int i = 0; i < userPlaylistsTotal; i++) {
            JSONObject playlistJSON = userPlaylistsJSONArray.getJSONObject(i);
            String playlistID = playlistJSON.getString("id");

            JButton loadButton = new JButton("load");
            loadButton.addActionListener(e -> {
                this.playlistJSON = APIHandler.generateCustomJSON(playlistID);
                this.populate();
            });

            JLabel playlistName = new JLabel(playlistJSON.getString("name"));
            playlistName.setVerticalAlignment(JLabel.BOTTOM);

            JPanel playlistsRow = new JPanel();
            playlistsRow.add(playlistName, JLabel.CENTER);
            playlistsRow.add(loadButton);
            playlistsRow.setPreferredSize(new Dimension(500, 50));

            userPlaylistsPanel.add(playlistsRow);
        }

        JDialog userPlaylistsFrame = new JDialog(this.getFrame(),"User Playlists");
        userPlaylistsFrame.add(userPlaylistsPanel, BorderLayout.CENTER);

        userPlaylistsFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        userPlaylistsFrame.setLocationRelativeTo(null);
        userPlaylistsFrame.setResizable(false);
        userPlaylistsFrame.pack();
        userPlaylistsFrame.setVisible(true);
    }

    public String getUserPlaylists() {
        return (APIHandler.getRequestResponse("https://api.spotify.com/v1/me/playlists"));
    }

    public void showUserInfo() {

        JSONObject userInfoJSON = new JSONObject(this.getUserProfile());
        JLabel profileImageIconLabel = null;

        try {
            URL profileImageURL = new URL(userInfoJSON.getJSONArray("images").getJSONObject(0).getString("url"));
            Image profileImage = ImageIO.read(profileImageURL);
            ImageIcon profileImageIcon = new ImageIcon(profileImage.getScaledInstance(100, 100, Image.SCALE_DEFAULT));
            profileImageIconLabel = new JLabel(profileImageIcon);
            profileImageIconLabel.setVerticalAlignment(JLabel.TOP);
        } catch (Exception e) {
        }

        JLabel name = new JLabel("Name: " + userInfoJSON.getString("display_name"), SwingConstants.CENTER);
        JLabel email = new JLabel("Email: " + userInfoJSON.getString("email"), SwingConstants.CENTER);
        JLabel id = new JLabel("ID: " + userInfoJSON.getString("id"), SwingConstants.CENTER);
        JLabel country = new JLabel("Country: " + userInfoJSON.getString("country"), SwingConstants.CENTER);
        JLabel subscription = new JLabel("Subscription: " + userInfoJSON.getString("type"), SwingConstants.CENTER);

        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.add(name);
        userInfoPanel.add(email);
        userInfoPanel.add(id);
        userInfoPanel.add(country);
        userInfoPanel.add(subscription);

        JDialog userInfoFrame = new JDialog(this.getFrame(),"User Information");
        userInfoFrame.add(userInfoPanel, BorderLayout.CENTER);
        userInfoFrame.add(profileImageIconLabel, BorderLayout.WEST);

        userInfoFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        userInfoFrame.setLocationRelativeTo(null);
        userInfoFrame.setResizable(false);
        userInfoFrame.pack();
        userInfoFrame.setVisible(true);
    }

    public void exportToJSON() {
        JFileChooser fc = new JFileChooser();
        int response = fc.showSaveDialog(null);

        if (response == JFileChooser.APPROVE_OPTION) {
            writeToFile(this.playlistJSON.toString(), new File(fc.getSelectedFile().getAbsolutePath() + ".txt"));
        }
    }

    public void loadPlaylistFromJSON() {
        JFileChooser fc = new JFileChooser();
        int response = fc.showOpenDialog(null);

        if (response == JFileChooser.APPROVE_OPTION) {
            if (this.clearPlaylistConfirmation()) {
                this.playlistJSON = new JSONArray(readFromFile(fc.getSelectedFile()));
                this.populate();
            }
        }
    }

    public static boolean validatePlaylistID(String playlistID) {
        if (playlistID.equals("")) {
            return (false);
        }

        String response = APIHandler.getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID);
        JSONObject responseJSON = new JSONObject(response);

        if (responseJSON.optJSONObject("error") != null) {
            return (false);
        }

        return (true);
    }

    public void promptPlaylistID() {
        JDialog prompt = new JDialog(this.getFrame(),"Playlist ID");
        prompt.getContentPane().add(new JLabel("Please Enter Public Playlist ID", SwingConstants.CENTER), BorderLayout.NORTH);

        JTextField playlistInput = new JTextField();
        playlistInput.addActionListener(e -> {

            SwingWorker sw = new SwingWorker() {
                @Override
                protected String doInBackground() {
                    playlistInput.setEnabled(false);
                    if (validatePlaylistID(playlistInput.getText())) {
                        if (PlaylistManager.this.clearPlaylistConfirmation()) {
                            PlaylistManager.this.playlistJSON = APIHandler.generateCustomJSON(playlistInput.getText());
                            PlaylistManager.this.populate();
                            prompt.dispose();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid Playlist ID", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                    return ("1");
                }

                @Override
                protected void done() {
                    playlistInput.setEnabled(true);
                }

            };

            sw.execute();
        });

        prompt.getContentPane().add(playlistInput, BorderLayout.CENTER);
        prompt.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        prompt.setLocationRelativeTo(null);
        prompt.setResizable(false);
        prompt.pack();
        prompt.setVisible(true);
    }

    public void clearPlaylist() {
        this.playlist = new Playlist(this);
        this.playlistScrollContainer.setViewportView(this.playlist);
        this.playlistScrollContainer.repaint();
        this.playlistScrollContainer.revalidate();
    }

    public boolean clearPlaylistConfirmation() {
        int isReplace = 0;
        if (!this.playlist.isEmpty()) {
            isReplace = JOptionPane.showConfirmDialog(null, "There is a playlist loaded. Replace it?", "Playlist Replacement", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        }

        if (isReplace == JOptionPane.YES_OPTION) {
            this.clearPlaylist();
            return (true);
        }

        return (false);
    }

    public void populate() {
        //progressBar
        ProgressBarDialog pbd = new ProgressBarDialog("Loading Tracks", new JProgressBar(0, this.playlistJSON.length()));

        SwingWorker sw = new SwingWorker() {
            @Override
            protected String doInBackground() {
                //Table Header - Visible Labels
                Track header = new Track(PlaylistManager.this.playlist, true, null);
                Track.populateHeaderTrack(header);
                
                PlaylistManager.this.header = header;
                PlaylistManager.this.playlistScrollContainer.setColumnHeaderView(header);

                //Table Body
                for (int i = 0; i < PlaylistManager.this.playlistJSON.length(); i++) {
                    JSONObject currentTrack = PlaylistManager.this.playlistJSON.getJSONObject(i);
                    Track newTrack = new Track(PlaylistManager.this.playlist, false, currentTrack);

                    Track.populateBodyTrack(newTrack, i);
                    newTrack.initializeTrack();

                    pbd.incrementValue();
                    PlaylistManager.this.playlist.repaint();
                    PlaylistManager.this.playlist.revalidate();
                }
                return ("");
            }

            @Override
            protected void done() {
                //prompt("Playlist Loader", "Completed Loaded Playlist");
            }
        };
        sw.execute();
    }

    public static String codeVerifier() {
        int length = 32;

        byte[] values = new byte[length];
        new SecureRandom().nextBytes(values);

        String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(values);

        return (verifier);
    }

    public static String codeChallenge(String verifier) {
        String returnCode = "";

        try {
            byte[] temp = verifier.getBytes("US-ASCII");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(temp, 0, temp.length);

            byte[] digest = md.digest();

            returnCode = Base64.getUrlEncoder().withoutPadding().encodeToString(digest).split("=")[0];

            System.out.println(returnCode);
        } catch (Exception e) {
        }


        return (returnCode);
    }

    public static String readFromFile(File file) {
        String text = "";
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                text = sc.nextLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (text);
    }

    public static void writeToFile(String json, File file) {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(json);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendToPlaylistJSON(JSONObject toAdd) {
        this.playlistJSON.put(toAdd);
    }

    public void removeFromPlaylistJSON(Track toRemove) {
        this.playlistJSON.remove(toRemove.getNumInt() - 1);
    }

    public static void clearCache() {
        File cache = new File("cache.txt");
        if (cache.exists()) {
            cache.delete();
        }
    }

    public static File getCache() {
        File cache = new File("cache.txt");
        try {
            if (cache.createNewFile()) {
                writeToFile((new JSONArray()).toString(), cache);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (cache);
    }

    public static String readFromCache() {
        String returnText = readFromFile(getCache());
        return (returnText);
    }

    public static void writeToCache(String track) {
        JSONArray cacheTracks = new JSONArray(readFromCache());
        cacheTracks.put(new JSONObject(track));
        writeToFile(cacheTracks.toString(), getCache());
    }

    public boolean isAuthorizedNotify() {
        if (this.authorizedToken == null) {
            JOptionPane.showMessageDialog(null, "Please Login to access this feature.", "Unauthorized Access", JOptionPane.ERROR_MESSAGE);
            return (false);
        } else {
            return (true);
        }
    }
}

class Help extends JDialog {

    public Help(PlaylistManager man) {
        super(man.getFrame(), "Help", true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setSize(500, 500);
        this.setLocationRelativeTo(null);

        JTextArea helpInstructions = new JTextArea();
        helpInstructions.setLineWrap(true);
        helpInstructions.setWrapStyleWord(true);
        String movingTracks = "If tracks are not moving, be sure that no column is being ordered as track movement is disabled when sorted by any column.  To disable, click on sorted column until no sort symbol is shown or click on the number column to reset order";
        String playingMusic = "If you have trouble playing music, make sure you have yt-dlp downloaded and have added it to your system path as this application uses it via command line.";
        String trackSeeds = "In the recommendation feature, the only required input is the track seeds which is simply just a comma-separated list of song ids.  You can get song ids by right clicking on any loaded track and clicking \"copy track id\".";
        String tournament = "The tournament feature places songs from the loaded playlist against one another and reorganizes the playlist based on your selection.  This was implemented to make ordering playlists more fun and to facilitate custom ordering on Spotify";
        String cache = "When you load playlists from Spotify for the first time, it may take awhile.  However, upon future loads, tracks that you have loaded in before will be saved onto the cache, expediting the overall loading process. (You may clear it in the file menu at anytime)";
        String others = "If you encounter any other problems, please contact me at ngawang30@gmail.com with issues";

        JList helpOptions = new JList(new String[]{"Moving Tracks", "Playing Music", "Track Seeds", "Tournament", "Cache", "Others"});
        helpOptions.addListSelectionListener(e -> {
            if (helpOptions.getSelectedValue().equals("Moving Tracks")) {
                helpInstructions.setText(movingTracks);
            }

            if (helpOptions.getSelectedValue().equals("Playing Music")) {
                helpInstructions.setText(playingMusic);
            }

            if (helpOptions.getSelectedValue().equals("Track Seeds")) {
                helpInstructions.setText(trackSeeds);
            }

            if (helpOptions.getSelectedValue().equals("Tournament")) {
                helpInstructions.setText(tournament);
            }

            if (helpOptions.getSelectedValue().equals("Cache")) {
                helpInstructions.setText(cache);
            }

            if (helpOptions.getSelectedValue().equals("Others")) {
                helpInstructions.setText(others);
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, helpOptions, helpInstructions);
        split.setResizeWeight(.2);

        this.add(split, BorderLayout.CENTER);

        this.setVisible(true);
    }
}
