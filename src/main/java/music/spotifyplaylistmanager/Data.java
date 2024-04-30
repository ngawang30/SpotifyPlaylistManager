package music.spotifyplaylistmanager;

import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.awt.Container;
import java.awt.Component;
import java.util.Arrays;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBox;
import java.util.Comparator;
import java.awt.Color;
import java.awt.event.MouseAdapter;

class Data extends JLabel{

    private final Track track;
    private final DataType type;
    private int sortStage;
    private final boolean header;
    private final GridBagConstraints constraints;
    private static Data start;
    private static Data end;

    public <E> Data(DataType type, E value, boolean isHeader, GridBagConstraints constraints, Track track) {
        if(value instanceof ImageIcon) this.setIcon((ImageIcon)value);
        else if(value instanceof String)this.setText(value.toString());
        
        this.setForeground(Color.WHITE);
        this.track = track;
        this.header = isHeader;
        this.type = type;
        this.constraints = constraints;
    }

    public MouseAdapter getColumnMouseAdapter(){
        MouseAdapter adapter = new MouseAdapter(){
            
        @Override
        public void mouseEntered(MouseEvent e) {
            end = (Data) e.getComponent();
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            int press = e.getButton();

            if (press == MouseEvent.BUTTON3) {
                Data.this.showColumnSelectionMenu(e.getX(), e.getY());
            } else {
                if(Playlist.getSortingData()== null) moveColumn(Data.this.track.getPlaylist());
            }

            Data.this.getParent().repaint();
            Data.this.getParent().revalidate();
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            start = (Data) e.getComponent();
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            int press = e.getButton();
            if (press == MouseEvent.BUTTON1) {
                if (Data.this.getText().equals("#")) {
                    Data.this.sortPrimary();
                } else {
                    Data.this.sort();
                }
                Data.this.track.getPlaylist().repaint();
                Data.this.track.getPlaylist().revalidate();
            }
        }
        };
    
        return(adapter);
    }

    public Track getTrack() {
        return track;
    }

    public DataType getDataType() {
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
        if(!this.getDataType().isSupportsSorting()) return;
        
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

        DataType type = this.type;
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
            DataType startType = Data.start.type;
            DataType endType = Data.end.type;
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



class ColumnCheckBox extends JCheckBox {

    public ColumnCheckBox(String name, Data columnData) {
        super(name, columnData.getDataType().isVisible());
        this.addActionListener(e -> {
            columnData.getDataType().setVisible(!columnData.getDataType().isVisible());
            columnData.getTrack().getPlaylist().toggleColumn(columnData.getDataType());
        });
    }
}
