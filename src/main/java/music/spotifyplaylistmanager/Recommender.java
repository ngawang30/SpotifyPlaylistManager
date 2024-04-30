package music.spotifyplaylistmanager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URL;
import java.awt.BorderLayout;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import java.awt.Color;
import javax.swing.BorderFactory;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JProgressBar;
import java.awt.Dimension;
import java.awt.Component;
import java.net.URLEncoder;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.SwingWorker;
import javax.swing.text.Document;
import java.util.ArrayList;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class Recommender extends JDialog {

    private PlaylistManager man;
    private static JsonArray recommendationsJSON;
    private JScrollPane mainScroll;
    private JPanel mainPanel;

    public Recommender(PlaylistManager man) {
        super(man.getFrame(), "Recommendation Generator");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(500, 500);
        this.setLocationRelativeTo(man.getFrame());
        ArrayList<InputField> inputs = new ArrayList();

        this.man = man;

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                man.setRecommender(null);
            }
        });

        //main panel
        mainPanel = new JPanel(new GridBagLayout());
        mainScroll = new JScrollPane();

        //Recommendation Settings
        JPanel SettingPanel = new JPanel(new GridBagLayout());
        this.add(SettingPanel, BorderLayout.CENTER);

        JLabel accousticness = new JLabel("Accousticness");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        SettingPanel.add(accousticness, c);
        InputField accousticnessTextField = new InputField("&target_acousticness=", "Decimal: 0-1", 0.0, 1.0);
        accousticnessTextField.setListener(accousticnessTextField::checkDouble);
        accousticnessTextField.setPreferredSize(new Dimension(30, 20));
        inputs.add(accousticnessTextField);
        c.gridx = 1;
        SettingPanel.add(accousticnessTextField, c);

        JLabel danceability = new JLabel("Danceability");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        SettingPanel.add(danceability, c);
        InputField danceabilityTextField = new InputField("&target_danceability=", "Decimal: 0-1", 0.0, 1.0);
        danceabilityTextField.setListener(danceabilityTextField::checkDouble);
        danceabilityTextField.setPreferredSize(new Dimension(30, 20));
        inputs.add(danceabilityTextField);
        c.gridx = 1;
        SettingPanel.add(danceabilityTextField, c);

        JLabel duration = new JLabel("Duration");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        SettingPanel.add(duration, c);
        InputField durationTextField = new InputField("&target_duration_ms=", "Integer: (seconds)", 0.0, 1.0);
        durationTextField.setListener(durationTextField::checkDuration);
        durationTextField.setPreferredSize(new Dimension(30, 20));
        inputs.add(durationTextField);
        c.gridx = 1;
        SettingPanel.add(durationTextField, c);

        JLabel energy = new JLabel("Energy");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        SettingPanel.add(energy, c);
        InputField energyTextField = new InputField("&target_energy=", "Decimal: 0-1", 0.0, 1.0);
        energyTextField.setListener(energyTextField::checkDouble);
        energyTextField.setPreferredSize(new Dimension(30, 20));
        inputs.add(energyTextField);
        c.gridx = 1;
        SettingPanel.add(energyTextField, c);

        JLabel instrumentalness = new JLabel("Instrumentalness");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        SettingPanel.add(instrumentalness, c);
        InputField instrumentalnessTextField = new InputField("&target_instrumentalness=", "Decimal: 0-1", 0.0, 1.0);
        instrumentalnessTextField.setListener(instrumentalnessTextField::checkDouble);
        instrumentalnessTextField.setPreferredSize(new Dimension(30, 20));
        inputs.add(instrumentalnessTextField);
        c.gridx = 1;
        SettingPanel.add(instrumentalnessTextField, c);

        JLabel liveness = new JLabel("Liveness");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        SettingPanel.add(liveness, c);
        InputField livenessTextField = new InputField("&target_liveness=", "Decimal: 0-1", 0.0, 1.0);
        livenessTextField.setListener(livenessTextField::checkDouble);
        livenessTextField.setPreferredSize(new Dimension(30, 20));
        inputs.add(livenessTextField);
        c.gridx = 1;
        SettingPanel.add(livenessTextField, c);

        JLabel loudness = new JLabel("Loudness");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 6;
        SettingPanel.add(loudness, c);
        InputField loudnessTextField = new InputField("&target_loudness=", "Decimal: 0-1", 0.0, 1.0);
        loudnessTextField.setListener(loudnessTextField::checkDouble);
        loudnessTextField.setPreferredSize(new Dimension(30, 20));
        inputs.add(loudnessTextField);
        c.gridx = 1;
        SettingPanel.add(loudnessTextField, c);

        JLabel popularity = new JLabel("Popularity");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 7;
        SettingPanel.add(popularity, c);
        InputField popularityTextField = new InputField("&target_popularity=", "Integer: 0-100", 0, 100);
        popularityTextField.setListener(popularityTextField::checkRange);
        popularityTextField.setPreferredSize(new Dimension(30, 20));
        inputs.add(popularityTextField);
        c.gridx = 1;
        SettingPanel.add(popularityTextField, c);

        JLabel speechiness = new JLabel("Speechiness");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 8;
        SettingPanel.add(speechiness, c);
        InputField speechinessTextField = new InputField("&target_speechiness=", "Decimal: 0-1", 0.0, 1.0);
        speechinessTextField.setListener(speechinessTextField::checkDouble);
        speechinessTextField.setPreferredSize(new Dimension(30, 20));
        inputs.add(speechinessTextField);
        c.gridx = 1;
        SettingPanel.add(speechinessTextField, c);

        JLabel valence = new JLabel("Valence");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 9;
        SettingPanel.add(valence, c);
        InputField valenceTextField = new InputField("&target_valence=", "Decimal: 0-1", 0.0, 1.0);
        valenceTextField.setListener(valenceTextField::checkDouble);
        valenceTextField.setPreferredSize(new Dimension(30, 20));
        inputs.add(valenceTextField);
        c.gridx = 1;
        SettingPanel.add(valenceTextField, c);

        JLabel trackSeeds = new JLabel("Track Seeds");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 10;
        SettingPanel.add(trackSeeds, c);
        InputField trackSeedsField = new InputField("&seed_tracks=", "List of track ids", 0, 0);
        trackSeedsField.setListener(trackSeedsField::checkTrackSeeds);
        trackSeedsField.setPreferredSize(new Dimension(30, 20));
        inputs.add(trackSeedsField);
        c.gridx = 1;
        SettingPanel.add(trackSeedsField, c);

        JButton submit = new JButton("Submit");
        submit.addActionListener(e -> {

            //check if track seeds are null
            if (trackSeedsField.getText().equals("")) {
                JOptionPane.showMessageDialog(null, "Please Enter Track IDs", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SwingWorker sw = new SwingWorker() {
                @Override
                protected String doInBackground() {
                    String query = "https://api.spotify.com/v1/recommendations?limit=100";

                    for (InputField in : inputs) {
                        if (in.queryAddition != null) {
                            query += in.query;
                            query += in.queryAddition;
                        }
                    }
                    String recommendationJson = APIHandler.getRequestResponse(query);
                    Recommender.recommendationsJSON = generateRecommendationJsonArray(recommendationJson);

                    populate(man);

                    return ("1");
                }

                @Override
                protected void done() {
                    Recommender.this.remove(SettingPanel);
                    Recommender.this.add(Recommender.this.mainScroll, BorderLayout.CENTER);
                    Recommender.this.repaint();
                    Recommender.this.revalidate();
                }
            };
            sw.execute();

        });
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 11;
        SettingPanel.add(submit, c);

        //Button
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton settings = new JButton("⚙");
        buttonRow.add(settings);
        this.add(buttonRow, BorderLayout.SOUTH);
        settings.addActionListener(e -> toggleSettings(this, SettingPanel, mainScroll));

        this.setVisible(true);
    }

    public PlaylistManager getMan() {
        return man;
    }

    public static JsonArray getRecommendationsJSON() {
        return recommendationsJSON;
    }

    public static void setRecommendationsJSON(JsonArray RecommendationsJSON) {
        Recommender.recommendationsJSON = RecommendationsJSON;
    }

    public JScrollPane getMainScroll() {
        return mainScroll;
    }

    public void setMainScroll(JScrollPane mainScroll) {
        this.mainScroll = mainScroll;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public static void toggleSettings(JDialog dialog, Component one, Component two) {
        if (dialog.isAncestorOf(one)) {
            dialog.remove(one);
            dialog.add(two, BorderLayout.CENTER);
        } else {
            dialog.remove(two);
            dialog.add(one, BorderLayout.CENTER);
        }

        dialog.repaint();
        dialog.revalidate();
    }

    public void populate(PlaylistManager man) {
        this.mainPanel = new JPanel(new GridBagLayout());
        this.mainScroll = new JScrollPane(mainPanel);
        this.mainScroll.getVerticalScrollBar().setUnitIncrement(50);

        ProgressBarDialog pbd = new ProgressBarDialog("Loading Recommendations", new JProgressBar(0, recommendationsJSON.size()));

        for (int i = 0; i < recommendationsJSON.size(); i++) {
            ImageIcon trackCover = null;
            JsonObject currentTrack = recommendationsJSON.get(i).getAsJsonObject();
            
            
            Track track = new Track(man, currentTrack);
            trackCover = track.getCoverImage();
            track.addMouseListener(track.getRecommendationMouseAdapter());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            Data numLabel = new Data(DataType.NUMBER, String.valueOf(i + 1), false, c, track);
            track.setNum(numLabel);
            numLabel.setPreferredSize(new Dimension(25, 25));
            track.add(numLabel, c);

            c = new GridBagConstraints();
            c.gridx = 1;
            Data trackCoverLabel = new Data(DataType.COVER, trackCover, false, c, track);
            track.setCover(trackCoverLabel);
            trackCoverLabel.setPreferredSize(new Dimension(100, 100));
            track.add(trackCoverLabel, c);

            c = new GridBagConstraints();
            c.gridx = 2;
            Data trackNameLabel = new Data(DataType.TRACK, currentTrack.get("trackName").getAsString(), false, c, track);
            track.setTrackName(trackNameLabel);
            trackNameLabel.setPreferredSize(new Dimension(150, 150));
            track.add(trackNameLabel, c);

            c = new GridBagConstraints();
            c.gridx = 3;
            Data artistLabel = new Data(DataType.ARTIST, currentTrack.get("trackArtist").getAsString(), false, c, track);
            track.setArtist(artistLabel);
            artistLabel.setPreferredSize(new Dimension(150, 150));
            track.add(artistLabel, c);

            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = i;
            mainPanel.add(track, c);
            pbd.incrementValue();
        }
    }

    public static JsonArray generateRecommendationJsonArray(String response) {
        JsonArray tracks = JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("tracks");
        JsonArray custom = new JsonArray();

        for (int i = 0; i < tracks.size(); i++) {
            custom.add((JsonElement) APIHandler.parseSpotifyInfo(tracks.get(i).getAsJsonObject()));
        }

        return (custom);
    }
}

class InputField<T> extends JTextField {

    String query;
    String queryAddition = null;
    T min;
    T max;

    public InputField(String query, String tooltip, T min, T max) {
        this.query = query;
        this.setToolTipText(tooltip);
        this.min = min;
        this.max = max;
    }

    public void setListener(Runnable function) {
        this.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                function.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                function.run();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                function.run();
            }
        });
    }

    public void checkTrackSeeds() {
        if (this.getText().equals("")) {
            queryAddition = null;
            this.setBorder(BorderFactory.createLineBorder(Color.RED));
        } else {
            queryAddition = URLEncoder.encode(this.getText());
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
    }
    
    public void checkDuration(){
        String text = this.getText();
        
        //Convert seconds to miliseconds
        if(text.equals("")) return;
        
        try{
            int seconds = Integer.parseInt(text);
            seconds = seconds * 1000;
            this.queryAddition = String.valueOf(seconds);
        } catch(NumberFormatException e){
            this.setBorder(BorderFactory.createLineBorder(Color.RED));
        }
    }

    public void checkDouble() {
        String text = this.getText();
        try{
            if (text.equals("") || Double.parseDouble(text) >= (Double) min && Double.parseDouble(text) <= (Double) max) {
                this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                this.queryAddition = text;
                if (text.equals("")) {
                    this.queryAddition = null;
                }
            } else {
                this.setBorder(BorderFactory.createLineBorder(Color.RED));
            }
        } catch (NumberFormatException e) {
            this.setBorder(BorderFactory.createLineBorder(Color.RED));
        }
    }

    public void checkRange() {
        String text = this.getText();
        try{
            if (text.equals("") || Integer.parseInt(text) >= (int) min && Integer.parseInt(text) <= (int) max) {
                this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                this.queryAddition = text;
                if (text.equals("")) {
                    this.queryAddition = null;
                }
            } else {
                this.setBorder(BorderFactory.createLineBorder(Color.RED));
            }
        }catch (NumberFormatException e) {
            this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
    }
}
