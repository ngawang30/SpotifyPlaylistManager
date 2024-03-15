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
	boolean numVisible = true;
	boolean artistTypeVisible = false;
	boolean artistCountryVisible = false;
	boolean artistGenderVisible = false;
	boolean isDeadVisible = false;
	boolean subAreaVisible = false;
	boolean languageVisible = false;
	boolean coverVisible = true;
	boolean trackNameVisible = true;
	boolean artistNameVisible = true;
	boolean releasedDateVisible = false;
	boolean explicitVisible = false;
	boolean durationVisible = false;
	boolean popularityVisible = false;
	
	
	public Playlist(){
		this.setLayout(new GridBagLayout());
	}
	
	public void toggleNumColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.num,currentTrack.num.constraints);
			else currentTrack.remove(currentTrack.num);
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void toggleCoverColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.trackCover,currentTrack.trackCover.constraints);
			else currentTrack.remove(currentTrack.trackCover);;
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void toggleTrackNameColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.trackName,currentTrack.trackName.constraints);
			else currentTrack.remove(currentTrack.trackName);
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void toggleArtistNameColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.artistName,currentTrack.artistName.constraints);
			else currentTrack.remove(currentTrack.artistName);
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void toggleReleasedDateColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.releasedDate,currentTrack.releasedDate.constraints);
			else currentTrack.remove(currentTrack.releasedDate);
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void toggleDurationColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.duration,currentTrack.duration.constraints);
			else currentTrack.remove(currentTrack.duration);
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void togglePopularityColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.popularity,currentTrack.popularity.constraints);
			else currentTrack.remove(currentTrack.popularity);
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void toggleExplicitColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.explicit,currentTrack.explicit.constraints);
			else currentTrack.remove(currentTrack.explicit);
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void toggleArtistTypeColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.artistType,currentTrack.artistType.constraints);
			else currentTrack.remove(currentTrack.artistType);
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void toggleArtistCountryColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.artistCountry,currentTrack.artistCountry.constraints);
			else currentTrack.remove(currentTrack.artistCountry);
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void toggleArtistGenderColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.artistGender,currentTrack.artistGender.constraints);
			else currentTrack.remove(currentTrack.artistGender);
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void toggleIsDeadColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.isDead,currentTrack.isDead.constraints);
			else currentTrack.remove(currentTrack.isDead);
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void toggleSubAreaColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.subArea,currentTrack.subArea.constraints);
			else currentTrack.remove(currentTrack.subArea);
		} 
		this.repaint();
		this.revalidate();
	}
	
	public void toggleLanguageColumn(boolean visible){
		Component [] children = this.getComponents();
		for(Component com: children) {
			Track currentTrack = (Track) com;
			if(visible) currentTrack.add(currentTrack.language,currentTrack.language.constraints);
			else currentTrack.remove(currentTrack.language);
		} 
		this.repaint();
		this.revalidate();
	}
	
}