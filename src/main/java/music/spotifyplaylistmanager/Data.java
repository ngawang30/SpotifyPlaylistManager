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
import java.util.Comparator;


class Data extends JLabel implements MouseListener{
	Track track;
	int sortStage = 0;
	boolean isHeader;
	boolean isVisible;
	GridBagConstraints constraints;
	static Data start;
	static Data end;
	
	public Data(String value, boolean isHeader, boolean visible, GridBagConstraints constraints, Track track){
		super(value);
		this.isVisible = visible;
		this.track = track;
		this.isHeader = isHeader;
		if(isHeader) this.addMouseListener(this);
		this.constraints = constraints;
	}
	
	public Data(ImageIcon picture, boolean isHeader, boolean visible, GridBagConstraints constraints, Track track){
		super(picture);
		this.isVisible = visible;
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
		if(this.getText().equals("#")){
			this.sortPrimary();
		} else {
			this.sort();
		}
		this.track.playlist.repaint();
		this.track.playlist.revalidate();
	}
	
	//Only for "#" Column Limited to Asc
	public void sortPrimary(){
		Data [] column = this.generateColumnArray();
		Playlist playlist = this.track.playlist;
		
		if(playlist.sortingData!=null){
			playlist.sortingData.sortStage = 0;
			playlist.sortingData = null;
		}
		
		Arrays.sort(column, new Comparator<Data>(){
			@Override
			public int compare(Data a, Data b){
				int aInt = Integer.parseInt(a.getText());
				int bInt = Integer.parseInt(b.getText());
				
				return(Integer.compare(aInt,bInt));
			}
		});
		
		for(int i = 0; i < column.length; i ++){
			Track currentTrack = column[i].track;	
			currentTrack.constraints.gridy = i+1;
			playlist.add(currentTrack,currentTrack.constraints);
		}
	}
	
	public void sort(){
		Data [] column = this.generateColumnArray();
		Playlist playlist = this.track.playlist;
		String increase = "\u2191";
		String decrease = "\u2193";
		Comparator stringComp = new Comparator<Data>(){
			public int compare(Data a, Data b){
					String aString = a.getText();
					String bString = b.getText();
					
					return(aString.compareTo(bString));
				}
		};
		
		if(playlist.sortingData != this){
			if(playlist.sortingData!=null){
				playlist.sortingData.setText(playlist.sortingData.getText().replace(increase,""));
				playlist.sortingData.setText(playlist.sortingData.getText().replace(decrease,""));
				playlist.sortingData.sortStage = 0;
				playlist.sortingData = null;
			}
			
			playlist.sortingData = this;
			
			Arrays.sort(column, stringComp);
			
			for(int i = 0; i < column.length; i ++){
				Track currentTrack = column[i].track;	
				currentTrack.constraints.gridy = i+1;
				playlist.add(currentTrack,currentTrack.constraints);
			}
			
			this.setText(this.getText() + increase);
			this.sortStage = 1;
		} else if(this.sortStage==1) {
			Arrays.sort(column, stringComp.reversed());
			
			for(int i = 0; i < column.length; i ++){
				Track currentTrack = column[i].track;	
				currentTrack.constraints.gridy = i+1;
				playlist.add(currentTrack,currentTrack.constraints);
			}
			
			this.setText(this.getText().replace(increase,decrease));
			this.sortStage = 2;
		} else if(this.sortStage == 2){
			this.sortStage = 0;
			this.setText(this.getText().replace(decrease,""));
			playlist.sortingData = null;
			this.track.num.sortPrimary();
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
			if(!currentTrack.isHeader) column[counter++] = currentTrack.findData(pos);
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
				Data tempOne = currentTrack.findData(startX);
				Data tempTwo = currentTrack.findData(endX);
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
	
	
	
	public void showColumnSelectionMenu(int xPos, int yPos){
		Track track = this.track;
		
		JPopupMenu pop = new JPopupMenu("ColumnSelection");
		
		ColumnCheckBox num = new ColumnCheckBox("Number",track.num);
		pop.add(num);
		
		ColumnCheckBox cover = new ColumnCheckBox("Cover",track.cover);
		pop.add(cover);
		
		ColumnCheckBox name = new ColumnCheckBox("Name", track.name);
		pop.add(name);
		
		ColumnCheckBox artist = new ColumnCheckBox("Artists",track.artist);
		pop.add(artist);
		
		ColumnCheckBox releasedDate = new ColumnCheckBox("Released Date",track.releasedDate);
		pop.add(releasedDate);
		
		ColumnCheckBox duration = new ColumnCheckBox("Duration",track.duration);
		pop.add(duration);
		
		ColumnCheckBox popularity = new ColumnCheckBox("Popularity",track.popularity);
		pop.add(popularity);
		
		ColumnCheckBox explicit = new ColumnCheckBox("Explicit",track.explicit);
		pop.add(explicit);
		
		ColumnCheckBox artistType = new ColumnCheckBox("Artist Type",track.artistType);
		pop.add(artistType);
		
		ColumnCheckBox artistCountry = new ColumnCheckBox("Artist Country",track.artistCountry);
		pop.add(artistCountry);
		
		ColumnCheckBox artistGender = new ColumnCheckBox("Artist Gender",track.artistGender);
		pop.add(artistGender);
		
		ColumnCheckBox isDead = new ColumnCheckBox("Deceased",track.isDead);
		pop.add(isDead);
		
		ColumnCheckBox subArea = new ColumnCheckBox("SubArea",track.subArea);
		pop.add(subArea);
		
		ColumnCheckBox language = new ColumnCheckBox("Language",track.language);
		pop.add(language);
		
		pop.repaint();
		pop.revalidate();
		pop.show(this,xPos,yPos);
	}
	
}

class ColumnCheckBox extends JCheckBox{
	public ColumnCheckBox(String name, Data columnData){
		super(name,columnData.isVisible);
		this.addActionListener(e -> {
			columnData.isVisible = !columnData.isVisible;
			columnData.track.playlist.toggleColumn(columnData);
		});
	}
}