package music.spotifyplaylistmanager;

import javax.swing.JPanel;
import javax.swing.JMenuItem;
import org.json.JSONObject;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.JPopupMenu;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.util.Map.Entry;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.net.URL;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

class Track extends JPanel {

    private PlaylistManager man;
    private ArrayList<TrackColor> palette;
    private Playlist playlist;
    private JSONObject trackJSON;
    private Data num;
    private Data cover;
    private Data explicit;
    private Data language;
    private Data artistType;
    private Data artistCountry;
    private Data subArea;
    private Data artistGender;
    private Data dead;
    private Data releasedDate;
    private Data duration;
    private Data popularity;
    private Data trackName;
    private Data artist;
    private GridBagConstraints constraints;
    private boolean header = false;
    private static int clicks;
    

    //Constructor for Recommendations
    public Track(PlaylistManager man, JSONObject trackJSON) {
        this.man = man;
        this.trackJSON = trackJSON;
        this.setLayout(new GridBagLayout());

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)) {
                    showTrackMenu(e.getX(), e.getY(), false);
                } else {
                    TimerTask clickTimer = new TimerTask() {
                        @Override
                        public void run() {
                            clicks = 0;
                        }
                    };

                    Timer clickInterval = new Timer();
                    clickInterval.schedule(clickTimer, 250);

                    clicks++;

                    if (clicks == 2) {
                        Track.this.man.getMusicPlayer().setQueue(Track.this);
                        man.getMusicPlayer().loadTrack(Track.this);
                        man.getMusicPlayer().play();
                    }
                }
            }
        });
    }

    //Constructor for Playlist
    public Track(Playlist playlist, boolean isHeader, JSONObject trackJSON) {
        this.trackJSON = trackJSON;
        this.setLayout(new GridBagLayout());
        this.header = isHeader;
        this.playlist = playlist;
        this.getWidth();

        if (!this.header) {
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    Track.this.playlist.setTo((Track) e.getSource());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    Playlist thisPlaylist = Track.this.playlist;

                    if (thisPlaylist.getSortingData() == null) {
                        Track.this.playlist.getFrom().insertTrack(Track.this.playlist.getTo());
                        Track.this.playlist.setFrom(null);
                        Track.this.playlist.setTo(null);
                        thisPlaylist.repaint();
                        thisPlaylist.revalidate();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    Track.this.playlist.setFrom((Track) e.getComponent());
                }

                @Override
                public void mouseClicked(MouseEvent e) {

                    if (SwingUtilities.isRightMouseButton(e)) {
                        showTrackMenu(e.getX(), e.getY(), true);
                    } else {
                        TimerTask clickTimer = new TimerTask() {
                            @Override
                            public void run() {
                                clicks = 0;
                            }
                        };

                        Timer clickInterval = new Timer();
                        clickInterval.schedule(clickTimer, 250);

                        clicks++;

                        if (clicks == 2) {
                            Track.this.playlist.getMan().getMusicPlayer().setQueue(Track.this);
                            Track.this.playlist.getMan().getMusicPlayer().loadTrack(Track.this);
                            Track.this.playlist.getMan().getMusicPlayer().play();
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (this.playlist != null) {
            int width = this.getWidth();
            int height = this.getHeight();

            Graphics2D g2 = (Graphics2D) g;

            if (!this.header) {

                Color a = this.palette.get(0);
                float[] aHSB = Color.RGBtoHSB(a.getRed(), a.getGreen(), a.getBlue(), null);
                Color b = this.palette.get(1);
                float[] bHSB = Color.RGBtoHSB(b.getRed(), b.getGreen(), b.getBlue(), null);

                Color bright;
                Color dark;

                if (aHSB[2] > bHSB[2]) {
                    bright = a;
                    dark = b;
                } else {
                    bright = b;
                    dark = a;
                }

                g2.setPaint(new GradientPaint(0, 0, dark, width, height, bright));
                g2.fillRect(0, 0, width, height);
            }
        }
    }

    public void setPalette(BufferedImage buff) {
        int height = buff.getHeight();
        int width = buff.getWidth();
        int dimension = Integer.min(height, width);
        int range = 100;

        HashMap<Integer, Integer> colorMap = new HashMap<>();

        for (int j = 0; j < dimension; j++) {
            int currentColor = buff.getRGB(j, j);
            boolean newColor = true;
            Object[] keys = colorMap.keySet().toArray();

            for (Object key : keys) {
                if (newColor) {
                    int currentColorComp = new Color((int) key).getRGB();
                    double diff = getEuclidDiff(new Color(currentColor), new Color(currentColorComp));
                    if (diff < range) {

                        newColor = false;
                        colorMap.put(currentColorComp, colorMap.get(currentColorComp) + 1);

                    }
                }
            }

            if (newColor) {
                colorMap.put(currentColor, colorMap.getOrDefault(currentColor, 0) + 1);
            }
        }
        this.palette = new ArrayList<>();

        for (Entry<Integer, Integer> entry : colorMap.entrySet()) {
            this.palette.add(new TrackColor(entry.getKey(), entry.getValue()));
        }

        this.palette.sort((TrackColor a, TrackColor b) -> (Integer.compare(b.getCounter(), a.getCounter())));
    }

    public double getEuclidDiff(Color a, Color b) {
        double returnValue = Math.sqrt(
                Math.pow(a.getRed() - b.getRed(), 2)
                + Math.pow(a.getGreen() - b.getGreen(), 2)
                + Math.pow(a.getBlue() - b.getBlue(), 2)
        );
        return (returnValue);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.getTrackName().getText() != null ? this.getTrackName().getText().hashCode() : 0);
        hash = 23 * hash + Objects.hashCode(this.getArtist().getText() != null ? this.getArtist().getText().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Track other = (Track) obj;
        if (this.getTrackName().getText().equals(other.getTrackName().getText()) && this.getArtist().getText().equals(other.getArtist().getText())) {
            return (true);
        }
        return (false);
    }

    public PlaylistManager getMan() {
        return man;
    }

    public void setMan(PlaylistManager man) {
        this.man = man;
    }

    public ArrayList<TrackColor> getPalette() {
        return palette;
    }

    public void setPalette(ArrayList<TrackColor> palette) {
        this.palette = palette;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public JSONObject getTrackJSON() {
        return trackJSON;
    }

    public void setTrackJSON(JSONObject trackJSON) {
        this.trackJSON = trackJSON;
    }

    public Data getNum() {
        return num;
    }

    public void setNum(Data num) {
        this.num = num;
    }

    public Data getCover() {
        return cover;
    }

    public void setCover(Data cover) {
        this.cover = cover;
    }

    public Data getExplicit() {
        return explicit;
    }

    public void setExplicit(Data explicit) {
        this.explicit = explicit;
    }

    public Data getLanguage() {
        return language;
    }

    public void setLanguage(Data language) {
        this.language = language;
    }

    public Data getArtistType() {
        return artistType;
    }

    public void setArtistType(Data artistType) {
        this.artistType = artistType;
    }

    public Data getArtistCountry() {
        return artistCountry;
    }

    public void setArtistCountry(Data artistCountry) {
        this.artistCountry = artistCountry;
    }

    public Data getSubArea() {
        return subArea;
    }

    public void setSubArea(Data subArea) {
        this.subArea = subArea;
    }

    public Data getArtistGender() {
        return artistGender;
    }

    public void setArtistGender(Data artistGender) {
        this.artistGender = artistGender;
    }

    public Data isDead() {
        return dead;
    }

    public void setDead(Data dead) {
        this.dead = dead;
    }

    public Data getReleasedDate() {
        return releasedDate;
    }

    public void setReleasedDate(Data releasedDate) {
        this.releasedDate = releasedDate;
    }

    public Data getDuration() {
        return duration;
    }

    public void setDuration(Data duration) {
        this.duration = duration;
    }

    public Data getPopularity() {
        return popularity;
    }

    public void setPopularity(Data popularity) {
        this.popularity = popularity;
    }

    public GridBagConstraints getConstraints() {
        return constraints;
    }

    public void setConstraints(GridBagConstraints constraints) {
        this.constraints = constraints;
    }

    public Data getTrackName() {
        return trackName;
    }

    public void setTrackName(Data trackName) {
        this.trackName = trackName;
    }

    public Data getArtist() {
        return artist;
    }

    public void setArtist(Data artist) {
        this.artist = artist;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public static int getClicks() {
        return clicks;
    }

    public static void setClicks(int clicks) {
        Track.clicks = clicks;
    }
    
    
    
    public static void populateHeaderTrack(Track header){
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data numHeaderLabel = new Data("#", true, true, c, header);
                numHeaderLabel.setPreferredSize(new Dimension(25, 50));
                header.num = numHeaderLabel;
                header.add(numHeaderLabel, c);

                c = new GridBagConstraints();
                c.gridx = 1;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data coverHeaderLabel = new Data("Cover", true, true, c, header);
                coverHeaderLabel.setPreferredSize(new Dimension(100, 50));
                header.cover = coverHeaderLabel;
                header.add(coverHeaderLabel, c);

                c = new GridBagConstraints();
                c.gridx = 2;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data trackNameHeaderLabel = new Data("Track", true, true, c, header);
                trackNameHeaderLabel.setPreferredSize(new Dimension(150, 50));
                header.setTrackName(trackNameHeaderLabel);
                header.add(trackNameHeaderLabel, c);

                c = new GridBagConstraints();
                c.gridx = 3;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data artistNameHeaderLabel = new Data("Artists", true, true, c, header);
                artistNameHeaderLabel.setPreferredSize(new Dimension(150, 50));
                header.artist = artistNameHeaderLabel;
                header.add(artistNameHeaderLabel, c);

                c = new GridBagConstraints();
                c.gridx = 4;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data durationHeaderLabel = new Data("Duration", true, true, c, header);
                durationHeaderLabel.setPreferredSize(new Dimension(150, 50));
                header.duration = durationHeaderLabel;
                header.add(durationHeaderLabel, c);

                //Table Header - Invisible Labels
                c = new GridBagConstraints();
                c.gridx = 5;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data releasedDateHeaderLabel = new Data("Released Date", true, false, c, header);
                releasedDateHeaderLabel.setPreferredSize(new Dimension(150, 50));
                header.releasedDate = releasedDateHeaderLabel;

                c = new GridBagConstraints();
                c.gridx = 6;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data popularityLabelHeaderLabel = new Data("Popularity", true, false, c, header);
                popularityLabelHeaderLabel.setPreferredSize(new Dimension(150, 50));
                header.popularity = popularityLabelHeaderLabel;

                c = new GridBagConstraints();
                c.gridx = 7;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data explicitLabelHeaderLabel = new Data("Explicit", true, false, c, header);
                explicitLabelHeaderLabel.setPreferredSize(new Dimension(150, 50));
                header.explicit = explicitLabelHeaderLabel;

                c = new GridBagConstraints();
                c.gridx = 8;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data artistTypeLabelHeaderLabel = new Data("Artist Type", true, false, c, header);
                artistTypeLabelHeaderLabel.setPreferredSize(new Dimension(150, 50));
                header.artistType = artistTypeLabelHeaderLabel;

                c = new GridBagConstraints();
                c.gridx = 9;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data artistCountryLabelHeaderLabel = new Data("Artist Country", true, false, c, header);
                artistCountryLabelHeaderLabel.setPreferredSize(new Dimension(150, 50));
                header.artistCountry = artistCountryLabelHeaderLabel;

                c = new GridBagConstraints();
                c.gridx = 10;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data artistGenderLabelHeaderLabel = new Data("Gender", true, false, c, header);
                artistGenderLabelHeaderLabel.setPreferredSize(new Dimension(150, 50));
                header.artistGender = artistGenderLabelHeaderLabel;

                c = new GridBagConstraints();
                c.gridx = 11;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data isDeadLabelHeaderLabel = new Data("Deceased", true, false, c, header);
                isDeadLabelHeaderLabel.setPreferredSize(new Dimension(150, 50));
                header.dead = isDeadLabelHeaderLabel;

                c = new GridBagConstraints();
                c.gridx = 12;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data subAreaLabelHeaderLabel = new Data("Sub-Location", true, false, c, header);
                subAreaLabelHeaderLabel.setPreferredSize(new Dimension(150, 50));
                header.subArea = subAreaLabelHeaderLabel;

                c = new GridBagConstraints();
                c.gridx = 13;
                c.weightx = 1;
                c.insets = new Insets(0, 3, 0, 3);
                Data languageLabelHeaderLabel = new Data("Language", true, false, c, header);
                languageLabelHeaderLabel.setPreferredSize(new Dimension(150, 50));
                header.language = languageLabelHeaderLabel;    
    }
    
    public static void populateBodyTrack(Track track, int num){
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data numLabel = new Data(Integer.toString(num + 1), false, true, c, track);
        numLabel.setPreferredSize(new Dimension(25, 25));
        track.num = numLabel;

        c = new GridBagConstraints();
        c.gridx = 1;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data coverLabel = new Data(track.getCoverImage(), false, true, c, track);
        coverLabel.setPreferredSize(new Dimension(100, 100));
        track.cover = coverLabel;

        c = new GridBagConstraints();
        c.gridx = 2;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data trackNameLabel = new Data(track.trackJSON.getString("trackName"), false, true, c, track);
        trackNameLabel.setPreferredSize(new Dimension(150, 150));
        track.setTrackName(trackNameLabel);

        c = new GridBagConstraints();
        c.gridx = 3;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data artistNameLabel = new Data(track.trackJSON.getString("trackArtist"), false, true, c, track);
        artistNameLabel.setPreferredSize(new Dimension(150, 150));
        track.artist = artistNameLabel;
        
        c = new GridBagConstraints();
        c.gridx = 4;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data durationLabel = new Data(track.trackJSON.getString("duration"), false, true, c, track);
        durationLabel.setPreferredSize(new Dimension(150, 150));
        track.duration = durationLabel;

        //Hidden
        c = new GridBagConstraints();
        c.gridx = 5;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data releasedDateLabel = new Data(track.trackJSON.getString("releasedDate"), false, false, c, track);
        releasedDateLabel.setPreferredSize(new Dimension(150, 150));
        track.releasedDate = releasedDateLabel;

        c = new GridBagConstraints();
        c.gridx = 6;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data popularityLabel = new Data(track.trackJSON.getString("popularity"), false, false, c, track);
        popularityLabel.setPreferredSize(new Dimension(150, 150));
        track.popularity = popularityLabel;

        c = new GridBagConstraints();
        c.gridx = 7;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data explicitLabel = new Data(track.trackJSON.getString("explicit"), false, false, c, track);
        explicitLabel.setPreferredSize(new Dimension(150, 150));
        track.explicit = explicitLabel;

        c = new GridBagConstraints();
        c.gridx = 8;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data artistTypeLabel = new Data(track.trackJSON.getString("artistType"), false, false, c, track);
        artistTypeLabel.setPreferredSize(new Dimension(150, 150));
        track.artistType = artistTypeLabel;

        c = new GridBagConstraints();
        c.gridx = 9;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data artistCountryLabel = new Data(track.trackJSON.getString("artistCountry"), false, false, c, track);
        artistCountryLabel.setPreferredSize(new Dimension(150, 150));
        track.artistCountry = artistCountryLabel;

        c = new GridBagConstraints();
        c.gridx = 10;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data artistGenderLabel = new Data(track.trackJSON.getString("artistGender"), false, false, c, track);
        artistGenderLabel.setPreferredSize(new Dimension(150, 150));
        track.artistGender = artistGenderLabel;

        c = new GridBagConstraints();
        c.gridx = 11;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data isDeadLabel = new Data(track.trackJSON.getString("isDead"), false, false, c, track);
        isDeadLabel.setPreferredSize(new Dimension(150, 150));
        track.dead = isDeadLabel;

        c = new GridBagConstraints();
        c.gridx = 12;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data subAreaLabel = new Data(track.trackJSON.getString("subArea"), false, false, c, track);
        subAreaLabel.setPreferredSize(new Dimension(150, 150));
        track.subArea = subAreaLabel;

        c = new GridBagConstraints();
        c.gridx = 13;
        c.weightx = 1;
        c.insets = new Insets(3, 3, 3, 3);
        Data languageLabel = new Data(track.trackJSON.getString("language"), false, false, c, track);
        languageLabel.setPreferredSize(new Dimension(150, 150));
        track.language = languageLabel;

        c = new GridBagConstraints();
        c.gridy = num + 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;
        track.constraints = c;
        track.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        track.playlist.add(track, c);
    }

    public ImageIcon getCoverImage() {
        ImageIcon trackCover = null;
        try {
            URL trackCoverUrl = new URL(this.trackJSON.getString("trackCoverURL"));
            BufferedImage trackCoverImage = ImageIO.read(trackCoverUrl);
            this.setPalette(trackCoverImage);

            trackCover = new ImageIcon(trackCoverImage.getScaledInstance(100, 100, Image.SCALE_DEFAULT));
        } catch (Exception e) {
        }

        return (trackCover);
    }

    public void showTrackMenu(int xPos, int yPos, boolean isPlaylist) {
        JPopupMenu pop = new JPopupMenu("ColumnSelection");

        if (!isPlaylist) {
            JMenuItem addToPlaylist = new JMenuItem("Add to Playlist");
            addToPlaylist.addActionListener(e -> {
                this.man.getPlaylist().addTrack(this);
            });
            pop.add(addToPlaylist);
        }

        if (isPlaylist) {
            JMenuItem removeFromPlaylist = new JMenuItem("Remove From Playlist");
            removeFromPlaylist.addActionListener(e -> {
                this.playlist.removeTrack(this);
            });
            pop.add(removeFromPlaylist);
        }

        pop.addSeparator();

        JMenuItem copyTrackName = new JMenuItem("Copy Track Name");
        copyTrackName.addActionListener(e -> {
            StringSelection string = new StringSelection(this.getNameString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(string, null);
        });
        pop.add(copyTrackName);

        JMenuItem copyID = new JMenuItem("Copy Track ID");
        copyID.addActionListener(e -> {
            StringSelection string = new StringSelection(this.trackJSON.getString("id"));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(string, null);
        });
        pop.add(copyID);

        pop.repaint();
        pop.revalidate();
        pop.show(this, xPos, yPos);
    }

    public String getNameString() {
        return (this.trackJSON.getString("trackName"));
    }

    public String getArtistString() {
        return (this.trackJSON.getString("trackArtist"));
    }

    public String getIDInt() {
        return (this.trackJSON.getString("id"));
    }

    public void setY(int y) {
        this.constraints.gridy = y;
    }

    public int getNumInt() {
        return (this.constraints.gridy);
    }

    public Data[] getRow() {
        Data[] row = new Data[14];

        row[0] = this.num;
        row[1] = this.cover;
        row[2] = this.explicit;
        row[3] = this.language;
        row[4] = this.artistType;
        row[5] = this.artistCountry;
        row[6] = this.subArea;
        row[7] = this.artistGender;
        row[8] = this.dead;
        row[9] = this.releasedDate;
        row[10] = this.duration;
        row[11] = this.popularity;
        row[12] = this.getTrackName();
        row[13] = this.artist;

        return (row);
    }

    public Data findData(int gridX) {
        Data[] data = this.getRow();
        Data toFind = null;

        for (Data data1 : data) {
            if (data1.getConstraints().gridx == gridX) {
                return data1;
            }
        }

        return (toFind);
    }

    public void initializeTrack() {
        this.add(this.num, this.num.getConstraints());
        this.add(this.cover, this.cover.getConstraints());
        this.add(this.getTrackName(), this.getTrackName().getConstraints());
        this.add(this.artist, this.artist.getConstraints());
        this.add(this.duration, this.duration.getConstraints());
    }

    public void swapTrack(Track toSwap) {
        //Swap in JSON
        this.playlist.getMan().swapJSONTracks(this.getNumInt() - 1, toSwap.getNumInt() - 1);

        //Swap in Component
        Playlist playlist = this.playlist;
        int posA = this.getNumInt();
        int posB = toSwap.getNumInt();

        this.setY(posB);
        this.num.setText(String.valueOf(posB));
        toSwap.setY(posA);
        toSwap.num.setText(String.valueOf(posA));
        playlist.add(this, this.constraints);
        playlist.add(toSwap, toSwap.constraints);
    }

    public void insertTrack(Track to) {
        if (this != to && this != null && to != null) {
            Playlist playlist = this.playlist;
            ArrayList<Track> tracks = new ArrayList(Arrays.asList(this.playlist.getTracks()));
            boolean topToBottom = this.getNumInt() < to.getNumInt();

            this.playlist.getMan().insertJSONTrack(this.getNumInt() - 1, to.getNumInt() - 1);

            tracks.remove(this);

            if (topToBottom) {
                tracks.add(tracks.indexOf(to) + 1, this);
            } else {
                tracks.add(tracks.indexOf(to), this);
            }

            playlist.cheapLoad(tracks);
        }
    }
}

class TrackColor extends Color {

    private int counter;

    public TrackColor(int rgb, int counter) {
        super(rgb);
        this.counter = counter;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}
