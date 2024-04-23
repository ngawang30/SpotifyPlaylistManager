package music.spotifyplaylistmanager;

import java.io.File;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import java.awt.Component;
import javax.swing.JOptionPane;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;

public class MusicPlayer extends JPanel {

    private SwingWorker sw;
    private Clip clip;
    private JLabel songName;
    private JLabel artist;
    private Track loadedTrack;
    private JPanel controls;
    private JLabel cover;
    private ArrayList<Track> queue;
    private JProgressBar pb;

    public MusicPlayer() {
        this.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        cover = new JLabel();
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weightx = 1;
        this.add(cover, c);
        
        
        c = new GridBagConstraints();
        this.songName = new JLabel(" ");
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 1;
        this.add(this.songName, c);
        
        
        pb = new JProgressBar();
        pb.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                double ratio = ((double) pb.getMousePosition().x) / pb.getWidth();
                long placement = (long) (clip.getMicrosecondLength() * ratio);
                clip.setMicrosecondPosition(placement);

            }
        });

        c = new GridBagConstraints();
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 2;
        this.add(pb, c);
        
        c = new GridBagConstraints();
        this.artist = new JLabel(" ");
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 3;
        this.add(this.artist, c);
        
        
        JButton skipBack = new JButton("⏪");
        skipBack.setPreferredSize(new Dimension(60, 20));
        skipBack.setFocusPainted(false);
        skipBack.addActionListener(e -> {
            int currentTrackIndex = queue.indexOf(loadedTrack);
            if (0 < queue.indexOf(loadedTrack)) {
                this.loadTrack(queue.get(currentTrackIndex - 1));
            }
        });
        this.add(skipBack, c);
        

        JButton play = new JButton("⏸");
        play.addActionListener(e -> {
            if (clip.isActive()) {
                clip.stop();
                play.setText("▶");
            } else {
                clip.start();
                play.setText("️⏸");
            }
        });
        play.setPreferredSize(new Dimension(70, 20));
        play.setFocusPainted(false);
        this.add(play, c);

        JButton skip = new JButton("⏩");
        skip.setPreferredSize(new Dimension(60, 20));
        skip.setFocusPainted(false);
        skip.addActionListener(e -> {
            MusicPlayer.this.nextTrack();
        });
        
        controls = new JPanel(new GridBagLayout());
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        controls.add(skipBack,c);
        c.gridx = 1;
        controls.add(play,c);
        c.gridx = 2;
        controls.add(skip,c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 4;
        c.weightx = .3;
        this.add(controls, c);
    }

    public Clip getClip() {
        return clip;
    }

    public void setClip(Clip clip) {
        this.clip = clip;
    }

    public JLabel getSongName() {
        return songName;
    }

    public void setSongName(JLabel songName) {
        this.songName = songName;
    }

    public JLabel getArtist() {
        return artist;
    }

    public void setArtist(JLabel artist) {
        this.artist = artist;
    }

    public Track getLoadedTrack() {
        return loadedTrack;
    }

    public void setLoadedTrack(Track loadedTrack) {
        this.loadedTrack = loadedTrack;
    }

    public JPanel getControls() {
        return controls;
    }

    public void setControls(JPanel controls) {
        this.controls = controls;
    }

    public JLabel getCover() {
        return cover;
    }

    public void setCover(JLabel cover) {
        this.cover = cover;
    }

    public ArrayList<Track> getQueue() {
        return queue;
    }

    public void setQueue(ArrayList<Track> queue) {
        this.queue = queue;
    }

    public JProgressBar getPb() {
        return pb;
    }

    public void setPb(JProgressBar pb) {
        this.pb = pb;
    }
    
    public void nextTrack(){
        int currentTrackIndex = queue.indexOf(loadedTrack);
        if (queue.size() > queue.indexOf(loadedTrack)) {
            this.loadTrack(queue.get(currentTrackIndex + 1));
        }
    }
    
    public void removeControls(){
        this.remove(controls);
    }

    public static String getSongURL(Track track) {
        String songURL = null;

        try {
            String query = (track.getNameString() + " " + track.getArtistString() + " lyrics").replaceAll("\\s+", "%20");
            Document doc = Jsoup.connect("https://www.google.com/search?tbm=vid&q=" + query).get();
            Element videoElement = doc.selectFirst("[data-surl]");
            songURL = videoElement.attr("data-surl");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        return (songURL);
    }

    public void setQueue(Track track) {
        this.queue = new ArrayList<>();

        Component[] tracks = track.getParent().getComponents();

        for (Component com : tracks) {
            Track currentTrack = (Track) com;
            if (!currentTrack.isHeader()) {
                this.queue.add(currentTrack);
            }
        }
    }

    public void loadTrack(Track track) {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        this.loadedTrack = track;
        int uniqueID = this.hashCode();

        String song = getSongURL(track);
        this.songName.setText(track.getNameString());
        this.artist.setText(track.getArtistString());
        this.repaint();
        this.revalidate();

        if(sw != null) sw.cancel(true);
        
        sw = new SwingWorker() {
            @Override
            protected String doInBackground() {
                synchronized(MusicPlayer.this){
                    try {
                        ProcessBuilder pbu = new ProcessBuilder("yt-dlp", "-x", "--audio-format", "wav", song, "--force-overwrite", "-o", uniqueID + ".wav").directory(new File("./res/temp/"));
                        Process p = pbu.start();
                        p.waitFor();

                        File file = new File("./res/temp/" +uniqueID + ".wav");
                        file.deleteOnExit();
                        try (AudioInputStream ais = AudioSystem.getAudioInputStream(file)) {
                            clip = AudioSystem.getClip();
                            clip.open(ais);
                        }

                        pb.setMaximum((int) clip.getMicrosecondLength());


                        clip.start();

                        try {
                            while (clip != null) {

                                pb.setValue((int) clip.getMicrosecondPosition());
                                pb.repaint();
                                pb.revalidate();
                                
                                if(clip.getFrameLength()==clip.getFramePosition()){
                                    clip = null;
                                }
                            }
                            
                            MusicPlayer.this.nextTrack();
                        } catch (NullPointerException e) {
                            pb.setValue(0);
                        }
                    } catch (InterruptedException e) {
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                return ("");
            }

            @Override
            protected void done() {}
        };
           
        sw.execute();
    }
    
    public void play(){
        if(this.clip!=null) this.clip.start();
    }
    
    public void pause(){
        if(this.clip!=null) this.clip.stop();
    }
}
