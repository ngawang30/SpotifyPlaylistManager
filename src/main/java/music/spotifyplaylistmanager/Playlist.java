package music.spotifyplaylistmanager;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.json.JSONObject;

class Playlist extends JPanel {

    private static Data sortingData = null;
    private Track from;
    private Track to;
    private PlaylistManager man;

    public Playlist(PlaylistManager man) {
        this.setLayout(new GridBagLayout());
        this.man = man;
    }

    public static Data getSortingData() {
        return sortingData;
    }

    public static void setSortingData(Data sortingData) {
        Playlist.sortingData = sortingData;
    }

    public Track getFrom() {
        return from;
    }

    public void setFrom(Track from) {
        this.from = from;
    }

    public Track getTo() {
        return to;
    }

    public void setTo(Track to) {
        this.to = to;
    }

    public PlaylistManager getMan() {
        return man;
    }

    public void setMan(PlaylistManager man) {
        this.man = man;
    }
    
    

    public boolean isEmpty() {
        if (this.getComponents().length != 0) {
            return (false);
        } else {
            return (true);
        }
    }

    public boolean isEmptyNotify() {
        if(this.isEmpty()){
             JOptionPane.showMessageDialog(null, "Playlist is empty.", "Error", JOptionPane.ERROR_MESSAGE);
             return(true);
        } else return(false);
    }
    
    public void cheapLoad(ArrayList<Track> tracks){
        this.removeAll();
        for (int i = 0; i < tracks.size(); i++) {
                Track currentTrack = tracks.get(i);
                currentTrack.getNum().setText(String.valueOf(i + 1));
                currentTrack.getConstraints().gridy = i + 1;
                this.add(currentTrack, currentTrack.getConstraints());
            }
        this.man.getFrame().repaint();
        this.man.getFrame().revalidate();
    }

    public Track[] getTracks() {
        Component[] components = this.getComponents();
        Track[] tracks = new Track[components.length];
        int counter = 0;

        for (Component com : components) {
            Track currentTrack = (Track) com;
            tracks[counter++] = currentTrack;
        }

        return (tracks);
    }
    
    public ArrayList<Track> getTracksArrayList(){
        return(new ArrayList(Arrays.asList(this.getTracks())));     
    }

    public ArrayList<JSONObject> getTracksJSONArrayList() {
        ArrayList<JSONObject> JSONTracks = new ArrayList();
        Iterator it = this.man.getPlaylistJSON().iterator();

        while (it.hasNext()) {
            JSONTracks.add(new JSONObject(it.next().toString()));
        }

        return (JSONTracks);
    }

    public void addTrack(Track toAdd) {
        String id = toAdd.getIDInt();
        JSONObject trackJSON = new JSONObject(APIHandler.getRequestResponse("https://api.spotify.com/v1/tracks/" + id));
        trackJSON = APIHandler.refineTrack(trackJSON);
        this.man.appendToPlaylistJSON(trackJSON);
        
        
        this.man.clearPlaylist();
        this.man.populate();
    }

    public void removeTrack(Track toRemove) {
        ArrayList<Track> tracks = this.getTracksArrayList();
        this.man.removeFromPlaylistJSON(toRemove);
       
        tracks.remove(toRemove);
        
        this.cheapLoad(tracks);
        this.repaint();
        this.revalidate();
    }

    public void toggleColumn(Data columnData) {
        int target = columnData.getConstraints().gridx;
        
        Track header = this.man.getHeader();
        Track[] tracks = this.getTracks();

        for (Track track : tracks) {
            Data dataInCurrentTrack = track.findData(target);

            if (columnData.isVisible()) {
                track.add(dataInCurrentTrack, dataInCurrentTrack.getConstraints());
            } else {
                track.remove(dataInCurrentTrack);
            }
        }
        
        Data headerData = header.findData(target);
        if(columnData.isVisible()) header.add(headerData,headerData.getConstraints());
        else header.remove(headerData);
            
        this.man.getFrame().repaint();
        this.man.getFrame().revalidate();
    }

}
