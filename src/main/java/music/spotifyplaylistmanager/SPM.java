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
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.lang.Double;


public class SPM {
	public static void main(String[] args) throws Exception{
		app();
	}
	
	
	
	public static void app (){
		//Playlist Body Component
		PlaylistManager man = new PlaylistManager();
		man.token = PlaylistManager.getToken();
		
		man.playlist = new Playlist(man);
		
		JPanel playlistContainer = new JPanel(new GridBagLayout());

		JScrollPane playlistContainerScroll = new JScrollPane();
		playlistContainerScroll.setViewportView(man.playlist);
		playlistContainerScroll.getVerticalScrollBar().setUnitIncrement(10);
		
		man.playlistScrollContainer = playlistContainerScroll;

		//Menu Component //Playlist Menu
		JMenuItem importSpotifyMenuItem = new JMenuItem("Import Playlist From Spotify");
		importSpotifyMenuItem.addActionListener(e -> PlaylistManager.promptPlaylistID(man));

		JMenuItem importJSONMenuItem = new JMenuItem("Import Playlist From JSON");
		importJSONMenuItem.addActionListener(e -> PlaylistManager.loadPlaylistFromJSON(man));


		JMenuItem exportJSONMenuItem = new JMenuItem("Export Playlist to JSON");
		exportJSONMenuItem.addActionListener(e -> PlaylistManager.exportToJSON(man));
		
		JMenuItem exportSpotifyMenuItem = new JMenuItem("Export Playlist to Spotify");
		//exportSpotifyMenuItem.addActionListener(e -> uploadPlaylist(man));
		
		
		
		JMenu playlistMenu = new JMenu("Playlist");
		playlistMenu.add(importSpotifyMenuItem);
		playlistMenu.add(importJSONMenuItem);
		playlistMenu.add(exportSpotifyMenuItem);
		playlistMenu.add(exportJSONMenuItem);
		
		//Menu Component //Profile Menu
		JMenuItem info = new JMenuItem("User Info");
		info.addActionListener(e -> PlaylistManager.showUserInfo(man));
		
		JMenuItem playlists = new JMenuItem("User Playlists");
		playlists.addActionListener(e -> PlaylistManager.showUserPlaylists(man));
		
		JMenuItem status = new JMenuItem("Login Status");
		status.addActionListener(e -> man.showStatus());
		
		JMenu profile = new JMenu("Profile");
		profile.add(info);
		profile.add(playlists);
		profile.add(status);
		
		JMenuItem openRecommendation = new JMenuItem("Open");
		openRecommendation.addActionListener(e -> Recommender.getRecommender(man));
		
		JMenu recommendationMenu = new JMenu("Recommendation");
		recommendationMenu.add(openRecommendation);
		
		
		//MenuBar Composition
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(playlistMenu);
		menuBar.add(profile);
		menuBar.add(recommendationMenu);
		

		//Application Component
		JFrame frame = new JFrame("Spotify Playlist Manager");

		frame.add(playlistContainerScroll,BorderLayout.CENTER);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500,500);
		frame.setLocationRelativeTo(null);

		frame.setJMenuBar(menuBar);
		frame.setVisible(true);
		
	}
}