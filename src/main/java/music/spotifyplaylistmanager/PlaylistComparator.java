package music.spotifyplaylistmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

//GetTracks may not be in order
public class PlaylistComparator extends JDialog{

    private PlaylistManager man;
    private ArrayList<Track> contenders;
    private int currentIndex;
    private ComparatorPlayer leftPlayer;
    private ComparatorPlayer rightPlayer;

    public PlaylistComparator(PlaylistManager man) {
        this.setSize(1000, 500);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.man = man;
        this.contenders = new ArrayList(Arrays.asList(man.getPlaylist().getTracks()));

        this.currentIndex = 0;

        leftPlayer = new ComparatorPlayer(this);
        rightPlayer = new ComparatorPlayer(this);

        this.load();

        JPanel comparatorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        comparatorPanel.add(leftPlayer, c);
        
        c.gridx = 1;
        comparatorPanel.add(rightPlayer, c);

        this.add(comparatorPanel, BorderLayout.CENTER);
        this.setVisible(true);
    }

    public PlaylistManager getMan() {
        return man;
    }

    public void setMan(PlaylistManager man) {
        this.man = man;
    }

    public ArrayList<Track> getContenders() {
        return contenders;
    }

    public void setContenders(ArrayList<Track> contenders) {
        this.contenders = contenders;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public ComparatorPlayer getLeftPlayer() {
        return leftPlayer;
    }

    public void setLeftPlayer(ComparatorPlayer leftPlayer) {
        this.leftPlayer = leftPlayer;
    }

    public ComparatorPlayer getRightPlayer() {
        return rightPlayer;
    }

    public void setRightPlayer(ComparatorPlayer rightPlayer) {
        this.rightPlayer = rightPlayer;
    }

    public void load() {
        int size = this.contenders.size();
        if (size <= 1) {
            this.dispose();
        } else {
            this.leftPlayer.track = contenders.get(currentIndex % size);
            this.leftPlayer.getCover().setIcon(leftPlayer.track.getCoverImage());
            this.rightPlayer.track = contenders.get((++currentIndex) % size);
            this.rightPlayer.getCover().setIcon(rightPlayer.track.getCoverImage());
        }
        
        this.leftPlayer.loadTrack(this.leftPlayer.track);
        this.rightPlayer.loadTrack(this.rightPlayer.track);

        this.repaint();
        this.revalidate();
        
    }

//    public void singleElimination(){
//        
//    }
}

class ComparatorPlayer extends MusicPlayer {

    PlaylistComparator pc;
    Track track;

    public ComparatorPlayer(PlaylistComparator pc) {
        super();
        this.setFocusable(true);
        this.removeControls();
        this.pc = pc;
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        this.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent e){
                ComparatorPlayer.this.play();
            }
            
            @Override
            public void mouseExited(MouseEvent e){
                ComparatorPlayer.this.pause();
            }
        });
        
        JLabel buffer = new JLabel(" ");
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 5;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(buffer, c);
        
        JButton picker = new JButton("Select");
        c = new GridBagConstraints();
        c.gridy = 6;
        c.gridwidth = 3;
        c.gridheight = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(picker, c);

        picker.addActionListener(e -> {
            Track leftTrack = this.pc.getLeftPlayer().track;
            Track rightTrack = this.pc.getRightPlayer().track;
            
            if (((JButton) e.getSource()).getParent() == this.pc.getLeftPlayer()) {
                //Swap if chosen is higher than the other
                if (leftTrack.getNumInt() > rightTrack.getNumInt()) {
                    leftTrack.insertTrack(rightTrack);
                }

                this.pc.getContenders().remove(this.pc.getRightPlayer().track);
            } else {
                if (leftTrack.getNumInt() < rightTrack.getNumInt()) {
                    rightTrack.insertTrack(leftTrack);
                }

                this.pc.getContenders().remove(this.pc.getLeftPlayer().track);
            }

            this.repaint();
            this.revalidate();
            this.pc.load();
        });
    }

}
