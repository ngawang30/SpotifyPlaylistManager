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
import java.util.Timer;
import java.util.TimerTask;


class Track extends JPanel implements MouseListener{
	Playlist playlist;
	boolean isHeader;
	Data num;
	Data trackCover;
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
	Data trackName;
	Data artistName;
	static Track start;
	static Track end;
	
	static Clip clip;
	static int clicks;
	static File file;
	
	public Track(int number, Playlist playlist, boolean isHeader){
		this.setLayout(new GridBagLayout());
		this.isHeader = isHeader;
		this.playlist = playlist;
		this.addMouseListener(this);
	}
	
	@Override
	public void mouseExited(MouseEvent e){}
	
	@Override
	public void mouseEntered(MouseEvent e){
		end = (Track) e.getComponent();
	}
	
	@Override
	public void mouseReleased(MouseEvent e){
		moveTrack(start, end);
		start = null;
		end = null;
		this.playlist.repaint();
		this.playlist.revalidate();
	}
	
	@Override
	public void mousePressed(MouseEvent e){
		start = (Track) e.getComponent();
	}
	
	@Override
	public void mouseClicked(MouseEvent e){
		TimerTask clickTimer = new TimerTask(){
			@Override
			public void run(){
				clicks = 0;
			}
		};
		
		Timer clickInterval = new Timer();
		clickInterval.schedule(clickTimer, 250);
		
		clicks++;
		
		if(clicks==2)this.playTrack();
	}
	
	public String getSongURL(){
		String songURL = null;
		try{
			String query = (this.trackName.getText() + " " + this.artistName.getText() + " lyrics").replaceAll("\\s+","%20");
			
			Document doc = Jsoup.connect("https://www.google.com/search?tbm=vid&q=" + query).get();
			Element videoElement = doc.selectFirst("[data-surl]");
			songURL = videoElement.attr("data-surl");
		} catch (Exception e){}
		
		return(songURL);
	}
	
	public void playTrack(){
		if(clip!=null) {
			clip.stop();
			clip.close();
			clip = null;
		} else {
			String song = this.getSongURL();
			
			
			SwingWorker sw = new SwingWorker(){
				@Override
				protected String doInBackground(){
					
					try{
						ProcessBuilder pb = new ProcessBuilder("yt-dlp", "-x", "--audio-format", "wav", song, "--force-overwrite","-o","temp.wav").directory(new File("."));
						Process p = pb.start();
						p.waitFor();
						
					} catch (Exception e){
						e.printStackTrace();
					}
					
					return ("");
				}
				
				@Override
				protected void done(){
					try{
						file = new File("temp.wav");
						AudioInputStream ais = AudioSystem.getAudioInputStream(file);
						clip = AudioSystem.getClip(); 
						clip.open(ais);				
						clip.start();
						ais.close();
					} catch (Exception e){}
				}
			};
			
			sw.execute();
			
		}
		
	}
	
	public void initializeTrack(){
		this.add(this.num,this.num.constraints);
		this.add(this.trackCover,this.trackCover.constraints);
		this.add(this.trackName,this.trackName.constraints);
		this.add(this.artistName,this.artistName.constraints);
	}
	
	public static void moveTrack(Track start, Track end){
		if(start!=end && start != null && end != null){
			Playlist playlist = start.playlist;
			
			
			int startPos = start.constraints.gridy;
			int top = end.constraints.gridy>start.constraints.gridy?start.constraints.gridy:end.constraints.gridy;
			int bottom = end.constraints.gridy<start.constraints.gridy?start.constraints.gridy:end.constraints.gridy;
			boolean topToBottom = end.constraints.gridy > start.constraints.gridy;
					
					//Move bottom track in place of target track
					start.constraints.gridy = end.constraints.gridy;
					start.num.setText(String.valueOf(start.constraints.gridy));
					playlist.add(start,start.constraints);
					
					//Shift all other tracks down
					Component [] Tracks = playlist.getComponents();
					
					for(int i = 0; i < Tracks.length; i++){
						Track currentTrack = (Track) Tracks[i];
						
						if(!currentTrack.isHeader && currentTrack.constraints.gridy > top && currentTrack.constraints.gridy < bottom){
							if(!topToBottom) {
								currentTrack.constraints.gridy++;
								currentTrack.num.setText(String.valueOf(currentTrack.constraints.gridy));
								playlist.add(currentTrack,currentTrack.constraints);
							} else {
								currentTrack.constraints.gridy--;
								currentTrack.num.setText(String.valueOf(currentTrack.constraints.gridy));
								playlist.add(currentTrack,currentTrack.constraints);
							}
						}
					}
					
					//readd target track below original location
					if(!topToBottom) end.constraints.gridy++;
					else end.constraints.gridy--;
					end.num.setText(String.valueOf(end.constraints.gridy));
					playlist.add(end,end.constraints);
		}
	}
	
}