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
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;

class Track extends JPanel{
	Playlist playlist;
	JSONObject trackJSON;
	Data num;
	Data cover;
	Data explicit;
	Data language;
	Data artistType;
	Data artistCountry;
	Data subArea;
	Data artistGender;
	Data isDead;
	Data releasedDate;
	Data duration;
	Data popularity;
	GridBagConstraints constraints;
	Data name;
	Data artist;
	static Track from;
	static Track to;
	boolean isHeader = false;
	static Clip clip;
	static int clicks;

	//Constructor for Recommendations
	public Track(PlaylistManager man, JSONObject trackJSON){
		this.trackJSON = trackJSON;
		this.setLayout(new GridBagLayout());
		
		this.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				int press = e.getButton();
				
				if(press==MouseEvent.BUTTON3){
					showTrackMenu(e.getX(),e.getY());
				} else {
					TimerTask clickTimer = new TimerTask(){
						@Override
						public void run(){
							clicks = 0;
						}
					};
					
					Timer clickInterval = new Timer();
					clickInterval.schedule(clickTimer, 250);
					
					clicks++;
					
					if(clicks==2){ 
						Track.this.playlist.man.mp.setQueue(Track.this);
						man.mp.playTrack(Track.this);
					}
				}
			}
		});
	}
	
	//Constructor for Playlist
	public Track(Playlist playlist, boolean isHeader, JSONObject trackJSON){
		this.trackJSON = trackJSON;
		this.setLayout(new GridBagLayout());
		this.isHeader = isHeader;
		this.playlist = playlist;
		
		this.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent e){
				to = (Track) e.getComponent();
			}
			
			@Override
			public void mouseReleased(MouseEvent e){
				Playlist thisPlaylist = Track.this.playlist;
				
				if(thisPlaylist.sortingData==null){
					moveTrack(from, to);
					from = null;
					to = null;
					thisPlaylist.repaint();
					thisPlaylist.revalidate();
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e){
				from = (Track) e.getComponent();
			}
			
			@Override
			public void mouseClicked(MouseEvent e){
				System.out.println(Track.this.getNum());
				
				int press = e.getButton();
				
				if(press==MouseEvent.BUTTON3){
					showTrackMenu(e.getX(),e.getY());
				} else {
					TimerTask clickTimer = new TimerTask(){
						@Override
						public void run(){
							clicks = 0;
						}
					};
					
					Timer clickInterval = new Timer();
					clickInterval.schedule(clickTimer, 250);
					
					clicks++;
					
					if(clicks==2) {
						Track.this.playlist.man.mp.setQueue(Track.this);
						Track.this.playlist.man.mp.playTrack(Track.this);
					}
				}
			}
		});
	}
	
	
	
	public void showTrackMenu(int xPos, int yPos){
		JPopupMenu pop = new JPopupMenu("ColumnSelection");
		
		JMenuItem copyTrackName = new JMenuItem("Copy Track Name");
		copyTrackName.addActionListener(e -> {
			StringSelection string = new StringSelection(this.getName());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(string,null);
		});
		pop.add(copyTrackName);
		
		JMenuItem copyID = new JMenuItem("Copy Track ID");
		copyID.addActionListener(e -> {
			StringSelection string = new StringSelection(this.trackJSON.getString("id"));
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(string,null);
		});
		pop.add(copyID);
		
		pop.repaint();
		pop.revalidate();
		pop.show(this,xPos,yPos);
	}
	
	public String getName(){
		return(this.trackJSON.getString("trackName"));
	}
	
	public String getArtist(){
		return(this.trackJSON.getString("trackArtist"));
	}
	
	public void setY(int y){
		this.constraints.gridy = y;
	}
	
	public int getNum(){
		return(this.constraints.gridy);
	}
	
	public Data[] getRow(){
		Data [] row = new Data[14];
		
		row[0] = this.num;
		row[1] = this.cover;
		row[2] = this.explicit;
		row[3] = this.language;
		row[4] = this.artistType;
		row[5] = this.artistCountry;
		row[6] = this.subArea;
		row[7] = this.artistGender;
		row[8] = this.isDead;
		row[9] = this.releasedDate;
		row[10] = this.duration;
		row[11] = this.popularity;
		row[12] = this.name;
		row[13] = this.artist;
		
		return(row);
	}
	
	public Data findData(int gridX){
		Data [] data = this.getRow();
		Data toFind = null;
		
		for(int i = 0; i < data.length; i++){
			if(data[i].constraints.gridx == gridX) return (data[i]);
		}
		
		return (toFind);
	}
	
	public void initializeTrack(){
		this.add(this.num,this.num.constraints);
		this.add(this.cover,this.cover.constraints);
		this.add(this.name,this.name.constraints);
		this.add(this.artist,this.artist.constraints);
	}
	
	public static void moveTrack(Track from, Track to){
		if(from!=to && from != null && to != null){
			Playlist playlist = from.playlist;
			ArrayList<Track> tracks = new ArrayList(Arrays.asList(from.playlist.getTracks()));
			boolean topToBottom = from.getNum()<to.getNum();
			
			from.playlist.man.moveJSONSong(from.getNum()-1,to.getNum()-1);
			
			tracks.remove(from);
			
			if(topToBottom) tracks.add(tracks.indexOf(to)+1,from);
			else tracks.add(tracks.indexOf(to),from);
			
			for(int i = 0; i < tracks.size(); i++){
				Track currentTrack = tracks.get(i);
				currentTrack.num.setText(String.valueOf(i+1));
				currentTrack.constraints.gridy = i+1;
				playlist.add(currentTrack,currentTrack.constraints);
			}
		}
	}
}