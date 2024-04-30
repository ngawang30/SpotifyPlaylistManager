package music.spotifyplaylistmanager;

//Command Pattern:Command
public interface SPMCommand{
    public void execute();
    public void undo();
}

class AddTrackSPMCommand implements SPMCommand{
    private Track track;
    private Playlist playlist;
    
    public AddTrackSPMCommand(Playlist playlist,Track track){
        this.playlist = playlist;
        this.track = track;
    }
    
    @Override
    public void execute(){
        playlist.addTrack(track);
        System.out.println("ADDED");
    }
    
    @Override
    public void undo(){
        playlist.removeTrack(track);
        System.out.println("REMOVED");
    }
}

class RemoveTrackSPMCommand implements SPMCommand{
    private Playlist playlist;
    private Track track;
    private int removalIndex;
    
    public RemoveTrackSPMCommand(Playlist playlist, Track track){
        this.playlist = playlist;
        this.track = track;
        this.removalIndex = track.getNumInt();
    }
    
    @Override
    public void execute() {
        playlist.removeTrack(track);
        System.out.println("REMOVED");
    }

    @Override
    public void undo() {
        playlist.addTrack(track);
        track.insertTrack(playlist.getTrackAt(removalIndex));
        System.out.println("UNDO REMOVE");
    }
}

class InsertTrackSPMCommand implements SPMCommand{
    private Track main;
    private Track target;
    private int from;
    
    public InsertTrackSPMCommand(Track main, Track target){
        this.main = main;
        this.from = main.getNumInt();
        this.target = target;
    }
    
    @Override
    public void execute(){
        main.insertTrack(target);
        System.out.println("INSERTED");
    }

    @Override
    public void undo() {
        main.insertTrack(main.getPlaylist().getTrackAt(from));
        System.out.println("UNDO INSERTED");
    }

}




