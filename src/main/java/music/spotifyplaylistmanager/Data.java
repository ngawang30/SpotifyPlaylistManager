package music.spotifyplaylistmanager;

import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.GridBagConstraints;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.Container;
import java.awt.Component;
import java.util.Arrays;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBox;
import java.util.Comparator;
import java.awt.Color;

class Data extends JLabel implements MouseListener {

    private Track track;
    private Type type;
    private int sortStage;
    private boolean header;
    private GridBagConstraints constraints;
    private static Data start;
    private static Data end;

    public Data(Type type, String value, boolean isHeader, GridBagConstraints constraints, Track track) {
        super(value);
        this.setForeground(Color.WHITE);
        this.track = track;
        this.header = isHeader;
        this.type = type;
        if (isHeader) {
            this.addMouseListener(this);
        }
        this.constraints = constraints;
    }

    public Data(Type type, ImageIcon picture, boolean isHeader, GridBagConstraints constraints, Track track) {
        super(picture);
        this.track = track;
        this.type = type;
        this.header = isHeader;
        this.constraints = constraints;
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        end = (Data) e.getComponent();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int press = e.getButton();

        if (press == MouseEvent.BUTTON3) {
            this.showColumnSelectionMenu(e.getX(), e.getY());
        } else {
            moveColumn(this.track.getPlaylist());
        }

        this.getParent().repaint();
        this.getParent().revalidate();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        start = (Data) e.getComponent();

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int press = e.getButton();
        if (press == MouseEvent.BUTTON1) {
            if (this.getText().equals("#")) {
                this.sortPrimary();
            } else {
                this.sort();
            }
            this.track.getPlaylist().repaint();
            this.track.getPlaylist().revalidate();
        }
    }

    public Track getTrack() {
        return track;
    }

    public Type getType() {
        return type;
    }

    public int getSortStage() {
        return sortStage;
    }

    public void setSortStage(int sortStage) {
        this.sortStage = sortStage;
    }

    public boolean isHeader() {
        return header;
    }

    public GridBagConstraints getConstraints() {
        return constraints;
    }

    public static Data getStart() {
        return start;
    }

    public static void setStart(Data start) {
        Data.start = start;
    }

    public static Data getEnd() {
        return end;
    }

    public static void setEnd(Data end) {
        Data.end = end;
    }

    //Only for "#" Column Limited to Asc
    public void sortPrimary() {
        Data[] column = this.generateColumnArray();
        Playlist playlist = this.track.getPlaylist();

        if (Playlist.getSortingData() != null) {
            Playlist.getSortingData().sortStage = 0;
            Playlist.setSortingData(null);
        }

        Arrays.sort(column, (Data a, Data b) -> {
            int aInt = Integer.parseInt(a.getText());
            int bInt = Integer.parseInt(b.getText());

            return (Integer.compare(aInt, bInt));
        });

        for (int i = 0; i < column.length; i++) {
            Track currentTrack = column[i].track;
            currentTrack.getConstraints().gridy = i + 1;
            playlist.add(currentTrack, currentTrack.getConstraints());
        }
    }

    public void sort() {
        Data[] column = this.generateColumnArray();

        Playlist playlist = this.track.getPlaylist();
        String increase = "\u2191";
        String decrease = "\u2193";
        Comparator stringComp = (Comparator<Data>) (Data a, Data b) -> {
            String aString = a.getText();
            String bString = b.getText();

            return (aString.compareTo(bString));
        };

        if (Playlist.getSortingData() != this) {
            if (Playlist.getSortingData() != null) {
                Playlist.getSortingData().setText(Playlist.getSortingData().getText().replace(increase, ""));
                Playlist.getSortingData().setText(Playlist.getSortingData().getText().replace(decrease, ""));
                Playlist.getSortingData().sortStage = 0;
                Playlist.setSortingData(null);
            }

            Playlist.setSortingData(this);

            Arrays.sort(column, stringComp);

            for (int i = 0; i < column.length; i++) {
                Track currentTrack = column[i].track;
                currentTrack.getConstraints().gridy = i + 1;
                playlist.add(currentTrack, currentTrack.getConstraints());
            }

            this.setText(this.getText() + increase);
            this.sortStage = 1;
        } else if (this.sortStage == 1) {
            Arrays.sort(column, stringComp.reversed());

            for (int i = 0; i < column.length; i++) {
                Track currentTrack = column[i].track;
                currentTrack.getConstraints().gridy = i + 1;
                playlist.add(currentTrack, currentTrack.getConstraints());
            }

            this.setText(this.getText().replace(increase, decrease));
            this.sortStage = 2;
        } else if (this.sortStage == 2) {
            this.sortStage = 0;
            this.setText(this.getText().replace(decrease, ""));
            Playlist.setSortingData(null);
            this.track.getNum().sortPrimary();
        }
    }

    @Override
    public String toString() {
        return (this.getText());
    }

    public Data[] generateColumnArray() {
        Playlist playlist = this.track.getPlaylist();

        Component[] tracks = playlist.getComponents();

        Type type = this.type;
        Data[] column = new Data[tracks.length];
        int counter = 0;

        for (Component track1 : tracks) {
            Track currentTrack = (Track) track1;
            column[counter++] = currentTrack.findData(type);
        }

        return (column);
    }

    public static void moveColumn(Container playlist) {
        if (start.header && end.header) {
            Type startType = Data.start.type;
            Type endType = Data.end.type;
            Track header = ((Playlist) playlist).getMan().getHeader();

            Component[] tracks = playlist.getComponents();

            for (Component com : tracks) {
                Track currentTrack = (Track) com;
                Data tempOne = currentTrack.findData(startType);
                Data tempTwo = currentTrack.findData(endType);
                swapDataX(currentTrack, tempOne, tempTwo);
            }

            swapDataX(header, header.findData(startType), header.findData(endType));

        }
    }

    public static void swapDataX(Track track, Data one, Data two) {
        //remove?
        track.remove(one);
        track.remove(two);

        int tempX = one.constraints.gridx;
        one.constraints.gridx = two.constraints.gridx;
        two.constraints.gridx = tempX;

        track.add(one, one.constraints);
        track.add(two, two.constraints);
    }

    public void showColumnSelectionMenu(int xPos, int yPos) {
        Track track = this.track;

        JPopupMenu pop = new JPopupMenu("ColumnSelection");

        ColumnCheckBox num = new ColumnCheckBox("Number", track.getNum());
        pop.add(num);

        ColumnCheckBox cover = new ColumnCheckBox("Cover", track.getCover());
        pop.add(cover);

        ColumnCheckBox name = new ColumnCheckBox("Name", track.getTrackName());
        pop.add(name);

        ColumnCheckBox artist = new ColumnCheckBox("Artists", track.getArtist());
        pop.add(artist);

        ColumnCheckBox releasedDate = new ColumnCheckBox("Released Date", track.getReleasedDate());
        pop.add(releasedDate);

        ColumnCheckBox duration = new ColumnCheckBox("Duration", track.getDuration());
        pop.add(duration);

        ColumnCheckBox popularity = new ColumnCheckBox("Popularity", track.getPopularity());
        pop.add(popularity);

        ColumnCheckBox explicit = new ColumnCheckBox("Explicit", track.getExplicit());
        pop.add(explicit);

        ColumnCheckBox artistType = new ColumnCheckBox("Artist Type", track.getArtistType());
        pop.add(artistType);

        ColumnCheckBox artistCountry = new ColumnCheckBox("Artist Country", track.getArtistCountry());
        pop.add(artistCountry);

        ColumnCheckBox artistGender = new ColumnCheckBox("Artist Gender", track.getArtistGender());
        pop.add(artistGender);

        ColumnCheckBox isDead = new ColumnCheckBox("Deceased", track.isDead());
        pop.add(isDead);

        ColumnCheckBox subArea = new ColumnCheckBox("SubArea", track.getSubArea());
        pop.add(subArea);

        ColumnCheckBox language = new ColumnCheckBox("Language", track.getLanguage());
        pop.add(language);

        pop.repaint();
        pop.revalidate();
        pop.show(this, xPos, yPos);
    }
}

enum Type {
    number(false, true),
    cover(false, true),
    track(true, true),
    artist(true, true),
    releasedData(false, false),
    duration(false, true),
    popularity(false, false),
    explicit(true, false),
    artistType(true, false),
    artistCountry(true, false),
    artistGender(true, false),
    subArea(true, false),
    language(true, false),
    deceased(true, false);

    private final boolean supportsFilter;
    private boolean visible;

    private Type(boolean supportsFilter, boolean visible) {
        this.supportsFilter = supportsFilter;
        this.visible = visible;
    }

    public boolean isSupportsFilter() {
        return supportsFilter;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}

class ColumnCheckBox extends JCheckBox {

    public ColumnCheckBox(String name, Data columnData) {
        super(name, columnData.getType().isVisible());
        this.addActionListener(e -> {
            columnData.getType().setVisible(!columnData.getType().isVisible());
            columnData.getTrack().getPlaylist().toggleColumn(columnData);
        });
    }
}
