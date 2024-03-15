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


class Data extends JLabel implements MouseListener{
	Track track;
	int sortStage = 0;
	boolean isHeader;
	GridBagConstraints constraints;
	static Data start;
	static Data end;
	
	public Data(String value, boolean isHeader, GridBagConstraints constraints, Track track){
		super(value);
		this.track = track;
		this.isHeader = isHeader;
		if(isHeader) this.addMouseListener(this);
		this.constraints = constraints;
	}
	
	public Data(ImageIcon picture, boolean isHeader, GridBagConstraints constraints, Track track){
		super(picture);
		this.track = track;
		this.isHeader = isHeader;
		this.constraints = constraints;
	}
	
	@Override
	public void mouseExited(MouseEvent e){}
	
	@Override
	public void mouseEntered(MouseEvent e){
		end = (Data) e.getComponent();
	
	}
	
	@Override
	public void mouseReleased(MouseEvent e){
		int press = e.getButton();
		
		if(press==MouseEvent.BUTTON3){
			this.showColumnSelectionMenu(e.getX(),e.getY());
		} else {
			moveColumn(this.track.playlist);
		}
		
		
		this.getParent().repaint();
		this.getParent().revalidate();
	}
	
	@Override
	public void mousePressed(MouseEvent e){
		start = (Data) e.getComponent();

	}
	
	@Override
	public void mouseClicked(MouseEvent e){
		this.sort();
		this.track.playlist.repaint();
		this.track.playlist.revalidate();
	}
	
	public void sort(){
		int colNum = this.constraints.gridx;
		Data [] column = this.generateColumnArray();
		Playlist playlist = this.track.playlist;
		Component [] tracks = this.track.playlist.getComponents();
		
		if(playlist.sortingData != this){
			if(playlist.sortingData!=null){
				playlist.sortingData.sortStage = 0;
				playlist.sortingData = null;
			}
			
			playlist.sortingData = this;
			
			for(int i = 0; i < column.length; i++){
				int maxIndex = i;
				
				for(int j = i+1; j < column.length; j++){
					Data current = column[j];
					if(current.getText().compareToIgnoreCase(column[maxIndex].getText()) > 0) {
						maxIndex = j;
					}
				}
				
				Data temp = column[i];
				column [i] = column[maxIndex];
				column[maxIndex] = temp;
			}
			
			for(int i = 0; i < column.length; i ++){
				Track currentTrack = column[i].track;	
				currentTrack.constraints.gridy = i+1;
				playlist.add(currentTrack,currentTrack.constraints);
			}
			
			this.sortStage = 1;
		} else if(this.sortStage==1) {
			for(int i = 0; i < column.length; i++){
				int maxIndex = i;
				
				for(int j = i+1; j < column.length; j++){
					Data current = column[j];
					if(current.getText().compareToIgnoreCase(column[maxIndex].getText()) < 0) {
						maxIndex = j;
					}
				}
				
				Data temp = column[i];
				column [i] = column[maxIndex];
				column[maxIndex] = temp;
			}
			
			for(int i = 0; i < column.length; i ++){
				Track currentTrack = column[i].track;	
				currentTrack.constraints.gridy = i+1;
				playlist.add(currentTrack,currentTrack.constraints);
			}
			
			this.sortStage = 0;
			playlist.sortingData = null;
		}
	}
	
	public Data [] generateColumnArray(){
		Playlist playlist = this.track.playlist;
		
		Component [] tracks = playlist.getComponents();
		
		int pos = this.constraints.gridx;
		Data [] column = new Data [tracks.length-1];
		int counter = 0;
		
		for(int i = 0; i < tracks.length; i++){
			Track currentTrack = (Track) tracks[i];
			if(!currentTrack.isHeader) column[counter++] = findData(pos, (Track)tracks[i]);
		}
		
		return (column);
	}
	
	public static void moveColumn(Container playlist){
		if(start.isHeader && end.isHeader){
			int startX = Data.start.constraints.gridx;
			int endX = Data.end.constraints.gridx;
			
			Component [] tracks = playlist.getComponents();
			
			for(Component com: tracks){
				Track currentTrack = (Track) com;
				Data tempOne = findData(startX, currentTrack);
				Data tempTwo = findData(endX, currentTrack);
				swapDataX(currentTrack, tempOne,tempTwo);
			}
		}
	}
	
	public static void swapDataX(Track track, Data one, Data two){
		//remove?
		track.remove(one);
		track.remove(two);
		
		int tempX = one.constraints.gridx;
		one.constraints.gridx = two.constraints.gridx;
		two.constraints.gridx = tempX;
			
		track.add(one,one.constraints);
		track.add(two,two.constraints);
	}
	
	public static Data findData(int gridX, JPanel track){
		Component [] trackComponents = track.getComponents();
		Data toFind = null;
		
		for(int i = 0; i < trackComponents.length; i++){
			Data currentData = (Data) trackComponents[i];
			if(currentData.constraints.gridx == gridX) return (currentData);
		}
		
		return (toFind);
	}
	
	public void showColumnSelectionMenu(int xPos, int yPos){
		Playlist playlist = this.track.playlist;
		
		JPopupMenu pop = new JPopupMenu("ColumnSelection");
		
		JCheckBox num = new JCheckBox("Number",playlist.numVisible);
		num.addActionListener(e -> {
			playlist.numVisible = !playlist.numVisible;
			playlist.toggleNumColumn(playlist.numVisible);
		});
		pop.add(num);
		
		
		JCheckBox cover = new JCheckBox("Cover",playlist.coverVisible);
		cover.addActionListener(e -> {
			playlist.coverVisible = !playlist.coverVisible;
			playlist.toggleCoverColumn(playlist.coverVisible);
		});
		pop.add(cover);
		
		
		JCheckBox name = new JCheckBox("Name",playlist.trackNameVisible);
		name.addActionListener(e -> {
			playlist.trackNameVisible = !playlist.trackNameVisible;
			playlist.toggleTrackNameColumn(playlist.trackNameVisible);
		});
		pop.add(name);
		
		
		JCheckBox artist = new JCheckBox("Artists",playlist.artistNameVisible);
		artist.addActionListener(e -> {
			playlist.artistNameVisible = !playlist.artistNameVisible;
			playlist.toggleArtistNameColumn(playlist.artistNameVisible);
		});
		pop.add(artist);
		
		JCheckBox releasedDate = new JCheckBox("Released Date",playlist.releasedDateVisible);
		releasedDate.addActionListener(e -> {
			playlist.releasedDateVisible = !playlist.releasedDateVisible;
			playlist.toggleReleasedDateColumn(playlist.releasedDateVisible);
		});
		pop.add(releasedDate);
		
		JCheckBox duration = new JCheckBox("Duration",playlist.durationVisible);
		duration.addActionListener(e -> {
			playlist.durationVisible = !playlist.durationVisible;
			playlist.toggleDurationColumn(playlist.durationVisible);
		});
		pop.add(duration);
		
		JCheckBox popularity = new JCheckBox("Popularity",playlist.popularityVisible);
		popularity.addActionListener(e -> {
			playlist.popularityVisible = !playlist.popularityVisible;
			playlist.togglePopularityColumn(playlist.popularityVisible);
		});
		pop.add(popularity);
		
		JCheckBox explicit = new JCheckBox("Explicit",playlist.explicitVisible);
		explicit.addActionListener(e -> {
			playlist.explicitVisible = !playlist.explicitVisible;
			playlist.toggleExplicitColumn(playlist.explicitVisible);
		});
		pop.add(explicit);
		
		JCheckBox artistType = new JCheckBox("Artist Type",playlist.artistTypeVisible);
		artistType.addActionListener(e -> {
			playlist.artistTypeVisible = !playlist.artistTypeVisible;
			playlist.toggleArtistTypeColumn(playlist.artistTypeVisible);
		});
		pop.add(artistType);
		
		JCheckBox artistCountry = new JCheckBox("Artist Country",playlist.artistCountryVisible);
		artistCountry.addActionListener(e -> {
			playlist.artistCountryVisible = !playlist.artistCountryVisible;
			playlist.toggleArtistCountryColumn(playlist.artistCountryVisible);
		});
		pop.add(artistCountry);
		
		JCheckBox artistGender = new JCheckBox("Artist Gender",playlist.artistGenderVisible);
		artistGender.addActionListener(e -> {
			playlist.artistGenderVisible = !playlist.artistGenderVisible;
			playlist.toggleArtistGenderColumn(playlist.artistGenderVisible);
		});
		pop.add(artistGender);
		
		JCheckBox isDead = new JCheckBox("Deceased",playlist.isDeadVisible);
		isDead.addActionListener(e -> {
			playlist.isDeadVisible = !playlist.isDeadVisible;
			playlist.toggleIsDeadColumn(playlist.isDeadVisible);
		});
		pop.add(isDead);
		
		JCheckBox subArea = new JCheckBox("SubArea",playlist.subAreaVisible);
		subArea.addActionListener(e -> {
			playlist.subAreaVisible = !playlist.subAreaVisible;
			playlist.toggleSubAreaColumn(playlist.subAreaVisible);
		});
		pop.add(subArea);
		
		JCheckBox language = new JCheckBox("Language",playlist.languageVisible);
		language.addActionListener(e -> {
			playlist.languageVisible = !playlist.languageVisible;
			playlist.toggleLanguageColumn(playlist.languageVisible);
		});
		pop.add(language);
		
		
		
		
		pop.repaint();
		pop.revalidate();
		pop.show(this,xPos,yPos);
	}
	
}