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
import java.awt.event.MouseAdapter;
import java.util.ArrayList;

public class MusicPlayer extends JPanel{
	Clip clip;
	JLabel songName;
	JLabel artist;
	AudioInputStream ais;
	Track loadedTrack;
	ArrayList<Track> queue; 
	JProgressBar pb;

	public MusicPlayer(){
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = .3;
		JButton skipBack = new JButton("⏪");
		skipBack.setPreferredSize(new Dimension(60,20));
		skipBack.setFocusPainted(false);
		skipBack.addActionListener(e -> {
			int currentTrackIndex = queue.indexOf(loadedTrack);
			if(0 < queue.indexOf(loadedTrack)){
				this.playTrack(queue.get(currentTrackIndex-1));
			}
		});
		this.add(skipBack,c);
		
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.weightx = .3;
		JButton play = new JButton("⏸");
		play.addActionListener(e -> {
			if(clip.isActive()){
				clip.stop();
				play.setText("▶");
			} else {
				clip.start();
				play.setText("️⏸");
			} 
		}); 
		play.setPreferredSize(new Dimension(70,20));
		play.setFocusPainted(false);
		this.add(play,c);
		
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 3;
		c.weightx = .3;
		JButton skip = new JButton("⏩");
		skip.setPreferredSize(new Dimension(60,20));
		skip.setFocusPainted(false);
		skip.addActionListener(e -> {
			int currentTrackIndex = queue.indexOf(loadedTrack);
			if(queue.size() > queue.indexOf(loadedTrack)){
				this.playTrack(queue.get(currentTrackIndex+1));
			}
		});
		this.add(skip,c);
		
		pb = new JProgressBar();
		pb.addMouseListener(new MouseAdapter(){
		
			@Override
			public void mouseClicked(MouseEvent e){
				
				double ratio = ((double)pb.getMousePosition().x)/pb.getWidth();
				long placement = (long) (clip.getMicrosecondLength()*ratio);
				clip.setMicrosecondPosition(placement);
				
			}
		});
		
		c = new GridBagConstraints();
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		
		this.add(pb,c);
		
		this.songName = new JLabel(" ");
		c = new GridBagConstraints();
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 0;
		this.add(this.songName,c);
		
		this.artist = new JLabel(" ");
		c = new GridBagConstraints();
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 2;
		this.add(this.artist,c);
	}
	
	public static String getSongURL(Track track){
		String songURL = null;
		
		try{
			String query = (track.getName() + " " + track.getArtist() + " lyrics").replaceAll("\\s+","%20");
			Document doc = Jsoup.connect("https://www.google.com/search?tbm=vid&q=" + query).get();
			Element videoElement = doc.selectFirst("[data-surl]");
			songURL = videoElement.attr("data-surl");
		} catch (Exception e){
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		return(songURL);
	}
	
	public void setQueue(Track track){
		this.queue = new ArrayList<Track>();
		
		Component[] tracks = track.getParent().getComponents();
		
		for(Component com: tracks){
			Track currentTrack = (Track) com;
			if(!currentTrack.isHeader) this.queue.add(currentTrack);
		}
	}
	
	public void playTrack(Track track){
		if(clip!=null) {
			clip.stop();
			clip.close();
			clip = null;
		}
			this.loadedTrack = track;
		
			String song = getSongURL(track);
			this.songName.setText(track.getName());
			this.artist.setText(track.getArtist());
			this.repaint();
			this.revalidate();
			
			
			SwingWorker sw = new SwingWorker(){
				@Override
				protected String doInBackground(){
					
					try{
						ProcessBuilder pbu = new ProcessBuilder("yt-dlp", "-x", "--audio-format", "wav", song, "--force-overwrite","-o","temp.wav").directory(new File("."));
						Process p = pbu.start();
						p.waitFor();
						
						File file = new File("temp.wav");
						AudioInputStream ais = AudioSystem.getAudioInputStream(file);
						clip = AudioSystem.getClip(); 
						clip.open(ais);				
						clip.start();
						ais.close();
					
						pb.setMaximum((int)clip.getMicrosecondLength());
						
						try{
							while(clip.isActive()){
								Thread.sleep(1000);
								pb.setValue((int)clip.getMicrosecondPosition());
								pb.repaint();
								pb.revalidate();
							}
						} catch (NullPointerException e) {
							pb.setValue(0);
						}						
					} catch (Exception e){
						e.printStackTrace();
					}
					
					return ("");
				}
				
				@Override
				protected void done(){
				
				}
			};
			
			sw.execute();
	}
	
	public void getMusicPlayer(){
		JFrame frame = new JFrame("Music Player");
		frame.add(this,BorderLayout.CENTER);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(300,200);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public void loadClip(Clip clip){
		this.clip = clip;
	}


}