package music.spotifyplaylistmanager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.JOptionPane;

class Playlist extends JPanel {

    private static Data sortingData;
    private ArrayList<Track> tracks;
    private Track from;
    private Track to;
    private PlaylistManager man;

    public Playlist(PlaylistManager man, ArrayList<Track> tracks) {
        this.setLayout(new GridBagLayout());
        this.tracks = tracks;
        this.man = man;
    }

    public static Data getSortingData() {
        return sortingData;
    }

    public static void setSortingData(Data sortingData) {
        Playlist.sortingData = sortingData;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = tracks;
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
        if (this.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Playlist is empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return (true);
        } else {
            return (false);
        }
    }

    public void cheapLoad(ArrayList<Track> tracks, boolean save) {
        this.removeAll();
        for (int i = 0; i < tracks.size(); i++) {
            Track currentTrack = tracks.get(i);
            currentTrack.getNum().setText(String.valueOf(i + 1));
            currentTrack.getConstraints().gridy = i + 1;
            this.add(currentTrack, currentTrack.getConstraints());
        }

        if (save) {
            this.setTracks(tracks);
        }

        this.man.getFrame().repaint();
        this.man.getFrame().revalidate();
    }

    public Track[] getLoadedTracks() {
        Component[] components = this.getComponents();
        Track[] tracks = new Track[components.length];
        int counter = 0;

        for (Component com : components) {
            Track currentTrack = (Track) com;
            tracks[counter++] = currentTrack;
        }

        return (tracks);
    }

    public ArrayList<Track> getLoadedTracksArrayList() {
        return (new ArrayList(Arrays.asList(this.getLoadedTracks())));
    }

    public ArrayList<JsonObject> getTracksJsonArrayList() {
        ArrayList<JsonObject> JSONTracks = new ArrayList();
        Iterator it = this.man.getPlaylistJSON().iterator();

        while (it.hasNext()) {
            JSONTracks.add(JsonParser.parseString(it.next().toString()).getAsJsonObject());
        }

        return (JSONTracks);
    }

    public void addTrack(Track toAdd) {
        String id = toAdd.getIDInt();
        JsonObject trackJSON = JsonParser.parseString(APIHandler.getRequestResponse("https://api.spotify.com/v1/tracks/" + id)).getAsJsonObject();
        trackJSON = (JsonObject) APIHandler.refineTrack(trackJSON);
        this.man.appendToPlaylistJSON(trackJSON);

        this.man.clearPlaylist();
        this.man.populate();
    }

    public void removeTrack(Track toRemove) {
        ArrayList<Track> tracks = this.getLoadedTracksArrayList();
        this.man.removeFromPlaylistJSON(toRemove);

        tracks.remove(toRemove);

        this.cheapLoad(tracks, true);
        this.repaint();
        this.revalidate();
    }

    public void toggleColumn(Data columnData) {
        Type target = columnData.getType();
        Track header = this.man.getHeader();
        ArrayList<Track> tracks = this.man.getPlaylist().tracks;

        for (Track track : tracks) {
            Data dataInCurrentTrack = track.findData(target);

            if (target.isVisible()) {
                track.add(dataInCurrentTrack, dataInCurrentTrack.getConstraints());
            } else {
                track.remove(dataInCurrentTrack);
            }
        }

        Data headerData = header.findData(target);
        if (target.isVisible()) {
            header.add(headerData, headerData.getConstraints());
        } else {
            header.remove(headerData);
        }

        this.man.getFrame().repaint();
        this.man.getFrame().revalidate();
    }
}
