package music.spotifyplaylistmanager;

import java.net.URL;
import java.awt.BorderLayout;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import org.json.JSONObject;
import org.json.JSONArray;
import java.time.Duration;
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


public class Recommender extends JFrame{
	static JSONArray RecommendationsJSON;
	static JScrollPane mainScroll;
	static JPanel mainPanel;
	
	public Recommender(PlaylistManager man){
		super("Recommendation Generator");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(500,500);
		this.setLocationRelativeTo(man.mainFrame);
		man.recom = this;
		
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosed(WindowEvent e){
				man.recom = null;
			}
		});
		
		//main panel
		mainPanel = new JPanel(new GridBagLayout()); 
		mainScroll = new JScrollPane();
		mainScroll.getVerticalScrollBar().setUnitIncrement(50);
		
		//Recommendation Settings
		JPanel panel = new JPanel(new GridBagLayout());
		this.add(panel,BorderLayout.CENTER);
		
		JLabel accousticness = new JLabel("Accousticness");
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(accousticness,c);
		InputField accousticnessTextField = new InputField("&target_acousticness=",0.0,1.0);
		accousticnessTextField.setPreferredSize(new Dimension(30,20));
		c.gridx = 1;
		panel.add(accousticnessTextField,c);
		
		
		JLabel danceability = new JLabel("Danceability");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		panel.add(danceability,c);
		InputField danceabilityTextField = new InputField("&target_danceability=",0.0,1.0);
		danceabilityTextField.setPreferredSize(new Dimension(30,20));
		c.gridx = 1;
		panel.add(danceabilityTextField,c);
		
		JLabel duration = new JLabel("Duration");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		panel.add(duration,c);
		InputField durationTextField = new InputField("&target_duration_ms=",0.0,1.0);
		durationTextField.setPreferredSize(new Dimension(30,20));
		c.gridx = 1;
		panel.add(durationTextField,c);
		
		JLabel energy = new JLabel("Energy");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		panel.add(energy,c);
		InputField energyTextField = new InputField("&target_energy=",0.0,1.0);
		energyTextField.setPreferredSize(new Dimension(30,20));
		c.gridx = 1;
		panel.add(energyTextField,c);
		
		JLabel instrumentalness = new JLabel("Instrumentalness");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		panel.add(instrumentalness,c);
		InputField instrumentalnessTextField = new InputField("&target_instrumentalness=",0.0,1.0);
		instrumentalnessTextField.setPreferredSize(new Dimension(30,20));
		c.gridx = 1;
		panel.add(instrumentalnessTextField,c);
		
		JLabel liveness = new JLabel("Liveness");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 5;
		panel.add(liveness,c);
		InputField livenessTextField = new InputField("&target_liveness=",0.0,1.0);
		livenessTextField.setPreferredSize(new Dimension(30,20));
		c.gridx = 1;
		panel.add(livenessTextField,c);
		
		JLabel loudness = new JLabel("Loudness");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 6;
		panel.add(loudness,c);
		InputField loudnessTextField = new InputField("&target_loudness=",0.0,1.0);
		loudnessTextField.setPreferredSize(new Dimension(30,20));
		c.gridx = 1;
		panel.add(loudnessTextField,c);
		
		JLabel popularity = new JLabel("Popularity");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 7;
		panel.add(popularity,c);
		InputField popularityTextField = new InputField("&target_popularity=",0.0,1.0);
		popularityTextField.setPreferredSize(new Dimension(30,20));
		c.gridx = 1;
		panel.add(popularityTextField,c);
		
		JLabel speechiness = new JLabel("Speechiness");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 8;
		panel.add(speechiness,c);
		InputField speechinessTextField = new InputField("&target_speechiness=",0.0,1.0);
		speechinessTextField.setPreferredSize(new Dimension(30,20));
		c.gridx = 1;
		panel.add(speechinessTextField,c);
		
		JLabel valence = new JLabel("Valence");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 9;
		panel.add(valence,c);
		InputField valenceTextField = new InputField("&target_valence=",0.0,1.0);
		valenceTextField.setPreferredSize(new Dimension(30,20));
		c.gridx = 1;
		panel.add(valenceTextField,c);
		
		JLabel trackSeeds = new JLabel("Track Seeds");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 10;
		panel.add(trackSeeds,c);
		InputField trackSeedsField = new InputField("&seed_tracks=");
		trackSeedsField.setPreferredSize(new Dimension(30,20));
		c.gridx = 1;
		panel.add(trackSeedsField,c);
		
		JButton submit = new JButton("Submit");
		submit.addActionListener(e -> {
			
			SwingWorker sw = new SwingWorker(){
				@Override
				protected String doInBackground(){
					String query = "https://api.spotify.com/v1/recommendations?limit=100";
			
					for(InputField in: InputField.existingInputs){
						if (in.queryAddition != null){
							query += in.query;
							query += in.queryAddition;
						}
					}
					RecommendationsJSON = generateRecommendationJSONArray(APIHandler.getRequestResponse(query));
					
					populate(man);
					
					return("1");
				}
				
				@Override
				protected void done(){
					Recommender.this.remove(panel);
					Recommender.this.add(mainScroll,BorderLayout.CENTER);
					Recommender.this.repaint();
					Recommender.this.revalidate();
				}
			};
			sw.execute();
			
			
			
			
		});
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 11;
		panel.add(submit,c);
		
		//Button
		JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton settings = new JButton("⚙");
		buttonRow.add(settings);
		this.add(buttonRow, BorderLayout.SOUTH);
		settings.addActionListener(e -> toggleSettings(this,panel,mainScroll));
		
		this.setVisible(true);
	}
	
	public static void toggleSettings(JFrame frame, Component one, Component two){
		if(frame.isAncestorOf(one)){
			frame.remove(one);
			frame.add(two,BorderLayout.CENTER);
		} else{
			frame.remove(two);
			frame.add(one,BorderLayout.CENTER);
		}
		
		frame.repaint();
		frame.revalidate();
	}
	
	public static void populate(PlaylistManager man){
		mainPanel = new JPanel(new GridBagLayout()); 
		mainScroll = new JScrollPane(mainPanel);
		
		ProgressBarDialog pbd = new ProgressBarDialog("Loading Recommendations", new JProgressBar(0,RecommendationsJSON.length()));
		
		for(int i = 0; i < RecommendationsJSON.length(); i++){
			ImageIcon trackCover = null;
			JSONObject currentTrack = RecommendationsJSON.getJSONObject(i);
			
			try{
				URL trackCoverUrl = new URL(currentTrack.getString("trackCoverURL"));
				Image trackCoverImage = ImageIO.read(trackCoverUrl);
				trackCover = new ImageIcon(trackCoverImage.getScaledInstance(100,100,Image.SCALE_DEFAULT));
			} catch (Exception e){}
			
			Track track = new Track(man, currentTrack);
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			JLabel numLabel = new JLabel(String.valueOf(i+1));
			numLabel.setPreferredSize(new Dimension(25,25));
			track.add(numLabel,c);
			
			c.gridx = 1;
			JLabel trackCoverLabel = new JLabel(trackCover);
			trackCoverLabel.setPreferredSize(new Dimension(100,100));
			track.add(trackCoverLabel,c);
			
			c.gridx = 2;
			JLabel trackNameLabel = new JLabel(currentTrack.getString("trackName"));
			trackNameLabel.setPreferredSize(new Dimension(150,150));
			track.add(trackNameLabel,c);
			
			c.gridx = 3;
			JLabel artistLabel = new JLabel(currentTrack.getString("trackArtist"));
			artistLabel.setPreferredSize(new Dimension(150,150));
			track.add(artistLabel,c);
			
			c.gridx = 0;
			c.gridy = i;
			mainPanel.add(track,c);
			pbd.incrementValue();
		}
	}
	
	public static JSONArray generateRecommendationJSONArray(String response){
		JSONArray tracks = new JSONObject(response).getJSONArray("tracks");
		JSONArray custom = new JSONArray();
		
		for(int i = 0; i < tracks.length(); i++){
			custom.put(PlaylistManager.parseTrackJSON(tracks.getJSONObject(i)));
		}
		
		return(custom);
	}
}

class InputField extends JTextField {
	static ArrayList <InputField> existingInputs = new ArrayList<InputField>();
	String query;
	String queryAddition = null;
	
	public InputField(String query, double min,double max){
		this.query = query; 
		this.setToolTipText("Double: 0-1");
		existingInputs.add(this);
		
		this.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent e){
			}
			
			@Override
			public void removeUpdate(DocumentEvent e){
				InputField.this.checkLimitDouble(min,max);
			}
			
			@Override
			public void insertUpdate(DocumentEvent e){
				InputField.this.checkLimitDouble(min,max);
			}
		});
	}
	
	public InputField(String query){
		this.query = query;
		this.setToolTipText("Required. Comma seperated list of track ids");
		existingInputs.add(this);
		
		this.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent e){
			}
			
			@Override
			public void removeUpdate(DocumentEvent e){
				InputField.this.inputUpdate();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e){
				InputField.this.inputUpdate();
			}
		});
	}
	
	public void inputUpdate(){
		if(!this.getText().equals("")){ 
			queryAddition = URLEncoder.encode(this.getText());
			this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}
		else {
			queryAddition = null;
			this.setBorder(BorderFactory.createLineBorder(Color.RED));
		}
	}
	
	public void checkLimitDouble(double min, double max){
		String text = this.getText();
		Document doc = this.getDocument();
		
		SwingUtilities.invokeLater( () -> {
			if(doc.getLength()>4){
				try{
					this.getDocument().remove(this.getDocument().getLength()-1,1);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		});
		
		try{
			
			if(text.equals("") || Double.parseDouble(text) >= min && Double.parseDouble(text) <= max){
				this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				this.queryAddition = text;
				if(text.equals("")) this.queryAddition = null;
			}
			else throw new Exception();
		} catch (Exception e){
			this.queryAddition = null;
			this.setBorder(BorderFactory.createLineBorder(Color.RED));
		}
	}

}