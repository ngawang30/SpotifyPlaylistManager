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


class Playlist extends JPanel {
	static Data sortingData = null;
	PlaylistManager man;
	
	public Playlist(PlaylistManager man){
		this.setLayout(new GridBagLayout());
		this.man = man;
	}
	
	public Track[] getTracks(){
		Component [] components = this.getComponents();
		Track [] tracks = new Track[components.length-1];  
		int counter = 0;
		
		for(Component com:components){
			Track currentTrack = (Track) com;
			if(!currentTrack.isHeader) tracks[counter++] = currentTrack;
		}

		return(tracks);
	}
	
	public void toggleColumn(Data columnData){
		int target = columnData.constraints.gridx;
		Component [] tracks = columnData.track.playlist.getComponents();
			
		for(Component com: tracks){
			Track currentTrack = (Track) com;
			
			if(columnData.isVisible) currentTrack.add(currentTrack.findData(target),currentTrack.findData(target).constraints);
			else currentTrack.remove(currentTrack.findData(target));
		}
		
		this.repaint();
		this.revalidate();
	}
	
}