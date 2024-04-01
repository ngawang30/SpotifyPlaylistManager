package music.spotifyplaylistmanager;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import org.json.JSONObject;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.JPopupMenu;
import javax.sound.sampled.Clip;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.Graphics;
import java.awt.Color;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Comparator;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

class Track extends JPanel{
	ArrayList<TrackColor> palette;
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
		
		if(!this.isHeader){
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
	}
	
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		
		if(this.playlist!=null){
			int x = this.getX();
			int y = this.getY();
			int width = this.getWidth();
			int height = this.getHeight();
			
			Graphics2D g2 = (Graphics2D) g;
			if(!this.isHeader) {
				g2.setPaint(new GradientPaint(0,0,this.palette.get(0),width,height,this.palette.get(1)));
				g2.fillRect(0,0,width,height);
			}
			
		}
		/* if(!this.isHeader){
			for(int i = 0; i < width; i++){
				for(int j = 0; j < height; j++){
					g.setColor(this.palette.get(0));
					g.drawRect(i,j,1,1);
				}
			}
		} */
	}
	
	public void setPalette(BufferedImage buff){
		int height = buff.getHeight();
		int width = buff.getWidth();
		int range = 100;
		int pixels = height * width;
		HashMap<Integer,Integer> colorMap = new HashMap<>();
		
			for(int j = 0; j < width; j++){
				int currentColor = buff.getRGB(j,j);
				boolean newColor = true;
				Object[] keys = colorMap.keySet().toArray();
				
				
				
				for(int i = 0; i < keys.length; i++){
					if(newColor){
						int currentColorComp = new Color((int)keys[i]).getRGB();
						double diff = getEuclidDiff(new Color(currentColor),new Color(currentColorComp));
						if(diff < range){
							
							//System.out.println(diff);
							newColor = false;
							colorMap.put(currentColorComp,colorMap.get(currentColorComp)+1);
							
						}
					}
				}
					
				if(newColor) colorMap.put(currentColor,colorMap.getOrDefault(currentColor,0)+1);
				
			}
		
		this.palette = new ArrayList<>();
		
		Iterator it = colorMap.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer, Integer> entry = (Entry<Integer,Integer>) it.next();
			this.palette.add(new TrackColor(entry.getKey(),entry.getValue()));
		}
		
		this.palette.sort(new Comparator<TrackColor>(){
			@Override
			public int compare(TrackColor a, TrackColor b){
				return(Integer.compare(b.counter,a.counter));
			}
		});
		
		Iterator ita = this.palette.iterator();
		
		while(ita.hasNext()) {
			TrackColor col = (TrackColor)ita.next();
		}
	}
	
	public double getEuclidDiff(Color a, Color b){
		double returnValue = Math.sqrt(
								Math.pow(a.getRed() - b.getRed(),2) + 
								Math.pow(a.getGreen() - b.getGreen(),2) + 
								Math.pow(a.getBlue() - b.getBlue(),2)
							);
		
		return (returnValue);
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

class TrackColor extends Color{
	int counter;
	
	public TrackColor(int rgb, int counter){
		super(rgb);
		this.counter = counter;
	}
	
}