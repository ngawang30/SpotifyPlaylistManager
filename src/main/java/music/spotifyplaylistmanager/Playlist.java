package music.spotifyplaylistmanager;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.Component;



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