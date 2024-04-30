package music.spotifyplaylistmanager;

import java.net.URL;
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
import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import java.util.ArrayList;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.JTextArea;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Base64;
import javax.swing.JFrame;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

//Invoker CommandPattern
class PlaylistManager {
    private final JFrame frame;
    private final SPMInvoker invoker;
    private Filter filter;
    private JScrollPane playlistScrollContainer;
    private Track header;
    private MusicPlayer musicPlayer;
    private JsonArray playlistJSON;
    private Playlist playlist;
    private String userID;
    private Recommender recommender;
    private static String authorizedToken;

    public PlaylistManager(JFrame frame, SPMInvoker invoker) {
        this.frame = frame;
        this.invoker = invoker;
    }

    public JFrame getFrame() {
        return frame;
    }

    public SPMInvoker getInvoker() {
        return invoker;
    }

    public Filter getFilter() {
        return filter;
    }
    
    public void setFilter(Filter filter){
        this.filter = filter;
    }

    public Recommender getRecommender() {
        return recommender;
    }

    public void setRecommender(Recommender recommender) {
        this.recommender = recommender;
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

    public JsonArray getPlaylistJSON() {
        return playlistJSON;
    }

    public void setPlaylistJSON(JsonArray playlistJSON) {
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

    public void swapJSONTracks(int from, int to) {
        ArrayList<JsonObject> JSONTracks = this.playlist.getTracksJsonArrayList();

        Collections.swap(JSONTracks, from, to);
        
        this.playlistJSON = new JsonArray();

        for (JsonObject jo : JSONTracks) {
            this.playlistJSON.add(jo);
        }
    }

    public void insertJSONTrack(int from, int to) {

        ArrayList<JsonObject> JSONTracks = this.playlist.getTracksJsonArrayList();

        JsonObject removed = JSONTracks.get(from);
        JSONTracks.remove(from);
        JSONTracks.add(to, removed);

        this.playlistJSON = new JsonArray();
        for (JsonObject jo : JSONTracks) {
            this.playlistJSON.add(jo);
        }
    }

    public String getUserProfile() {
        return (APIHandler.getRequestResponse("https://api.spotify.com/v1/me"));
    }

    public void uploadPlaylist(){
        String playlistID = createPlaylist(this);
        JsonArray currentPlaylist = this.playlistJSON;
        int size = currentPlaylist.size();
        int countSize = 0;


        while(countSize <= size){
                JsonArray trackUris = new JsonArray();

                for (int i = countSize; i <countSize+100 && i < size; i++){
                        trackUris.add(currentPlaylist.get(i).getAsJsonObject().get("uri"));
                }

                JsonObject trackUriObject = new JsonObject();
                trackUriObject.add("uris",trackUris);

                try{
                HttpRequest addtrackNameRequest = HttpRequest.newBuilder()
                        .uri(new URI("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks"))
                        .headers("Authorization","Bearer " + this.authorizedToken)
                        .POST(HttpRequest.BodyPublishers.ofString(trackUriObject.toString()))
                        .build();

                        HttpResponse<String> response = HttpClient.newHttpClient().send(addtrackNameRequest, HttpResponse.BodyHandlers.ofString());
                } catch(Exception e){}
                countSize += 100;
        }
    }
    
    public static String createPlaylist(PlaylistManager man) {
        String responseBody = null;
        JsonObject requestBody = new JsonObject();
        requestBody.add("name", JsonParser.parseString("default"));

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
        
        //get playlist id
        String playlistID = JsonParser.parseString(responseBody).getAsJsonObject().get("id").getAsString();

        return (playlistID);
    }
    
    public void showUserPlaylists() {
        JsonObject userPlaylistsJSON = JsonParser.parseString(this.getUserPlaylists()).getAsJsonObject();
        int userPlaylistsTotal = userPlaylistsJSON.get("total").getAsInt();
        JsonArray userPlaylistsJsonArray = userPlaylistsJSON.getAsJsonArray("items");

        JPanel userPlaylistsPanel = new JPanel();
        userPlaylistsPanel.setLayout(new BoxLayout(userPlaylistsPanel, BoxLayout.Y_AXIS));

        JPanel playlistsRowHeader = new JPanel();
        playlistsRowHeader.add(new JLabel("Playlists"));
        userPlaylistsPanel.add(playlistsRowHeader);

        for (int i = 0; i < userPlaylistsTotal; i++) {
            JsonObject playlistJSON = userPlaylistsJsonArray.get(i).getAsJsonObject();
            String playlistID = playlistJSON.get("id").getAsString();

            JButton loadButton = new JButton("load");
            loadButton.addActionListener(e -> {
                this.playlistJSON = APIHandler.generateCustomJSON(playlistID);
                this.populate();
            });

            JLabel playlistName = new JLabel(playlistJSON.get("name").getAsString());
            playlistName.setVerticalAlignment(JLabel.BOTTOM);

            JPanel playlistsRow = new JPanel();
            playlistsRow.add(playlistName, JLabel.CENTER);
            playlistsRow.add(loadButton);
            playlistsRow.setPreferredSize(new Dimension(500, 50));

            userPlaylistsPanel.add(playlistsRow);
        }

        JDialog userPlaylistsFrame = new JDialog(this.getFrame(), "User Playlists");
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

        JsonObject userInfoJSON = JsonParser.parseString(this.getUserProfile()).getAsJsonObject();
        JLabel profileImageIconLabel = null;

        try {
            URL profileImageURL = new URL(userInfoJSON.getAsJsonArray("images").get(0).getAsJsonObject().get("url").getAsString());
            Image profileImage = ImageIO.read(profileImageURL);
            ImageIcon profileImageIcon = new ImageIcon(profileImage.getScaledInstance(100, 100, Image.SCALE_DEFAULT));
            profileImageIconLabel = new JLabel(profileImageIcon);
            profileImageIconLabel.setVerticalAlignment(JLabel.TOP);
        } catch (Exception e) {
        }

        JLabel name = new JLabel("Name: " + userInfoJSON.get("display_name"), SwingConstants.CENTER);
        JLabel email = new JLabel("Email: " + userInfoJSON.get("email"), SwingConstants.CENTER);
        JLabel id = new JLabel("ID: " + userInfoJSON.get("id"), SwingConstants.CENTER);
        JLabel country = new JLabel("Country: " + userInfoJSON.get("country"), SwingConstants.CENTER);
        JLabel subscription = new JLabel("Subscription: " + userInfoJSON.get("type"), SwingConstants.CENTER);

        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.add(name);
        userInfoPanel.add(email);
        userInfoPanel.add(id);
        userInfoPanel.add(country);
        userInfoPanel.add(subscription);

        JDialog userInfoFrame = new JDialog(this.getFrame(), "User Information");
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

        try{
            if (response == JFileChooser.APPROVE_OPTION) {
                if (this.clearPlaylistConfirmation()) {
                    this.playlistJSON = JsonParser.parseString(readFromFile(fc.getSelectedFile())).getAsJsonArray();
                    this.populate();
                }
            }
        } catch (JsonSyntaxException e){
            JOptionPane.showMessageDialog(null, "Invalid File", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static boolean validatePlaylistID(String playlistID) {
        if (playlistID.equals("")) {
            return (false);
        }

        String response = APIHandler.getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID);
        JsonObject responseJSON = JsonParser.parseString(response).getAsJsonObject();

//        if (responseJSON.optJsonObject("error") != null) {
//            return (false);
//        }
        return (true);
    }

    public void promptPlaylistID() {
        JDialog prompt = new JDialog(this.getFrame(), "Playlist ID");
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
        this.playlist = new Playlist(this, new ArrayList());
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
        ProgressBarDialog pbd = new ProgressBarDialog("Loading Tracks", new JProgressBar(0, this.playlistJSON.size()));

        SwingWorker sw = new SwingWorker() {
            @Override
            protected String doInBackground() {
                //Table Header - Visible Labels
                Track header = new Track(PlaylistManager.this.playlist, null);
                Track.populateHeaderTrack(header);

                PlaylistManager.this.header = header;
                PlaylistManager.this.playlistScrollContainer.setColumnHeaderView(header);

                //Table Body
                for (int i = 0; i < PlaylistManager.this.playlistJSON.size(); i++) {
                    JsonObject currentTrack = PlaylistManager.this.playlistJSON.get(i).getAsJsonObject();
                    Track newTrack = new Track(PlaylistManager.this.playlist, currentTrack);

                    newTrack.populateBodyTrack();
                    newTrack.initializeTrack();
                    newTrack.setLoaded(true);
                    PlaylistManager.this.playlist.getTracks().add(newTrack);

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

    public void appendToPlaylistJSON(JsonObject toAdd) {
        this.playlistJSON.add(toAdd);
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
                writeToFile((new JsonArray()).toString(), cache);
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
        JsonArray cacheTracks = JsonParser.parseString(readFromCache()).getAsJsonArray();
        cacheTracks.add(JsonParser.parseString(track));
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
        String movingTracks = "If tracks are not moving, be sure that no column is being ordered as track movement is disabled when sorted by any column.  "
                + "To disable, click on sorted column until no sort symbol is shown or click on the number column to reset order";
        String playingMusic = "If you have trouble playing music, make sure you have yt-dlp downloaded and have added it to your system path as this application uses it via command line.";
        String Recommendation = "The recommendation feature allows you to specify song qualities such as accousticness, danceability, energy, etc.  "
                + "Most have a scale of 0 to 1.0, but to make sure, you can hover over them to see valid inputs."
                + "However, the only required input is the track seeds which is simply just a comma-separated list of song ids."
                + "You can get song ids by right clicking on any loaded track and clicking \"copy track id\".";
        String tournament = "The tournament feature places songs from the loaded playlist against one another and reorganizes the playlist based on your selection.  "
                + "This was implemented to make ordering playlists more fun and to facilitate custom ordering on Spotify";
        String cache = "When you load playlists from Spotify for the first time, it may take awhile.  "
                + "However, upon future loads, tracks that you have loaded in before will be saved onto the cache, expediting the overall loading process. "
                + "(You may clear it in the file menu at anytime)";
        String filter = "In order to filter, active the filter bar via the menu item in the functions menu.  "
                + "Then, using the visible column names, compose the filter like so: columnName:\"query\"";
        String others = "If you encounter any other problems, please contact me at ngawang30@gmail.com with issues";

        JList helpOptions = new JList(new String[]{"Moving Tracks", "Playing Music", "Track Seeds", "Tournament", "Cache", "Filter", "Others"});
        helpOptions.addListSelectionListener(e -> {
            if (helpOptions.getSelectedValue().equals("Moving Tracks")) {
                helpInstructions.setText(movingTracks);
            }

            if (helpOptions.getSelectedValue().equals("Playing Music")) {
                helpInstructions.setText(playingMusic);
            }

            if (helpOptions.getSelectedValue().equals("Track Seeds")) {
                helpInstructions.setText(Recommendation);
            }

            if (helpOptions.getSelectedValue().equals("Tournament")) {
                helpInstructions.setText(tournament);
            }

            if (helpOptions.getSelectedValue().equals("Cache")) {
                helpInstructions.setText(cache);
            }
            
            if (helpOptions.getSelectedValue().equals("Filter")) {
                helpInstructions.setText(filter);
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
