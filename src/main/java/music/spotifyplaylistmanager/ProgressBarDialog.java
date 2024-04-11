package music.spotifyplaylistmanager;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class ProgressBarDialog extends JDialog {

    private JProgressBar pb;

    public ProgressBarDialog(String progress, JProgressBar pb) {
        super(new JDialog(), progress, false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setSize(250, 50);
        this.setLocationRelativeTo(null);
        this.pb = pb;
        pb.setStringPainted(true);
        pb.setString("0%");

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                ProgressBarDialog.this.requestFocus();
            }
        });

        pb.addChangeListener(e -> {
            if (pb.getValue() >= pb.getMaximum()) {
                ProgressBarDialog.this.dispose();
            }
        });
        this.add(pb, BorderLayout.CENTER);
        this.setVisible(true);
    }

    public JProgressBar getPb() {
        return pb;
    }

    public void setPb(JProgressBar pb) {
        this.pb = pb;
    }

    public void incrementValue() {
        pb.setValue(pb.getValue() + 1);
        double percentage = (((double) pb.getValue()) / pb.getMaximum()) * 100;
        pb.setString(String.format("%2.0f%%", percentage));
        this.repaint();
        this.revalidate();
    }

    public void setValue(int val) {
        pb.setValue(val);
        double percentage = (((double) pb.getValue()) / pb.getMaximum()) * 100;
        pb.setString(String.format("%2.0f%%", percentage));
        this.repaint();
        this.revalidate();
    }
}
