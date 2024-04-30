package music.spotifyplaylistmanager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.awt.BorderLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class SPM {
    
    public SPM(){
        FlatDarkLaf.setup();
        JFrame frame = new JFrame("Spotify Playlist Manager");
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

        PlaylistManager man = new PlaylistManager(frame, new SPMInvoker());
        man.setFilter(new Filter(man));
        man.setPlaylist(new Playlist(man, new ArrayList()));
        man.setMusicPlayer(new MusicPlayer());
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
                man.uploadPlaylist();
                System.out.println("asd");
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
        //Questionable usage as members cannot login without being added to spotify application
        JMenuItem status = new JMenuItem("Login in");
        status.addActionListener(e -> {
            PlaylistManager.setAuthorizedToken(APIHandler.getAuthorizedToken());
            if (PlaylistManager.getAuthorizedToken() != null) {
                status.setEnabled(false);
                status.setText("Logged In");
                
                String userProfileInfo = man.getUserProfile();
                JsonElement userProfile = JsonParser.parseString(userProfileInfo);
                String id = userProfile.getAsJsonObject().getAsJsonPrimitive("id").getAsString();
                man.setUserID(id);
            }
        });
        
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
        
        JMenu profile = new JMenu("Profile");
        profile.add(status);
        profile.add(info);
        profile.add(playlists);
        

        //Menu Component //Recommendation
        JRadioButtonMenuItem filter = new JRadioButtonMenuItem("Filter");
        filter.addActionListener(e -> {
            if (man.getFilter().isActive()) {
                man.getFilter().setActive(false);

                frame.remove(man.getFilter());
            } else {
                filter.setEnabled(true);
                man.getFilter().setActive(true);
                frame.add(man.getFilter(), BorderLayout.NORTH);
            }

            frame.revalidate();
            frame.repaint();
        });

        JMenuItem recommendation = new JMenuItem("Recommendation");
        recommendation.addActionListener(e -> {
            if (man.isAuthorizedNotify()) {
                if(man.getRecommender()==null) man.setRecommender(new Recommender(man));
            }
        });

        JMenuItem playlistTournament = new JMenuItem("Tournament");
        playlistTournament.addActionListener(e -> {
            if (!man.getPlaylist().isEmptyNotify()) {
                new PlaylistComparator(man);
            }
        });

        JMenu functions = new JMenu("Functions");
        functions.add(filter);
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
            Window[] windows = frame.getOwnedWindows();
            String lookAndFeel = UIManager.getLookAndFeel().getName();
            if (lookAndFeel.equals("FlatLaf Dark")) {
                FlatLightLaf.setup();
            } else {
                FlatDarkLaf.setup();
            }

            SwingUtilities.updateComponentTreeUI(frame);

            for (Window win : windows) {
                SwingUtilities.updateComponentTreeUI(win);
            }

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

    //Three points of storage {Instance variable, JSONArray, GUI}
    public static void main(String[] args) {
        new SPM();
    }
}
