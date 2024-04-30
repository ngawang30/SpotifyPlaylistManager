package music.spotifyplaylistmanager;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Filter extends JPanel {
    //Syntax:  [field]:"[value]"
    private final PlaylistManager man;
    private final JTextField inputField;
    private boolean active;
    private static HashMap<String,String> toEnum;

    public Filter(PlaylistManager man) {
        super(new GridBagLayout());
        this.setBorder(BorderFactory.createEtchedBorder());
        this.man = man;
        
        GridBagConstraints c = new GridBagConstraints();
        inputField = new JTextField();

        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                Filter.this.filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                Filter.this.filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                Filter.this.filter();
            }
        });

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        this.add(inputField, c);
        
//        toEnum = new HashMap(){{
//            put("track","TRACK");
//            put("artist","ARTIST");
//            put("release","ARTIST");
//        }};
    }

    public JTextField getInputField() {
        return inputField;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static String getFilterType(String filter) {
        return (filter.split(":")[0]);
    }

    public static String getFilterValue(String filter) {
        return (filter.split(":")[1]);
    }

    public void filter() {
        ArrayList<String> filters = this.getFilters();
        ArrayList<String> validFilters = new ArrayList();

        if (!this.man.getPlaylist().isEmptyNotify()) {
            ArrayList<Track> filteringList = new ArrayList(this.man.getPlaylist().getTracks());

            for (String s : filters) {
                if (this.isFilter(s)) {
                    validFilters.add(s);
                }
            }

            for (String s : validFilters) {

                String filterDataName = getFilterType(s).toUpperCase();
                String filterValue = getFilterValue(s).replaceAll("\"", "").toLowerCase();
                Data filterData;

                if (!filterValue.isEmpty()) {
                    for (int i = 0; i < filteringList.size(); i++) {
                        Track currentTrack = filteringList.get(i);
                        filterData = currentTrack.findData(DataType.valueOf(filterDataName));

                        if (!(filterData.toString().toLowerCase().contains(filterValue))) {
                            filteringList.remove(currentTrack);
                            i--;
                        }
                    }
                }
            }

            this.man.getPlaylist().cheapLoad(filteringList, false);
        }
    }

    public boolean isFilter(String filter) {
        String[] sepFilter = filter.split(":");
        String filterType = sepFilter[0].toUpperCase();

        //not formatted
        if (sepFilter.length == 0) {
            return (false);
        }

        //of enumeration
        try {
            //check if filter target column is visible
            if (!this.man.getHeader().findData(DataType.valueOf(filterType)).getDataType().isVisible()) {
                return (false);
            }

            //disallow non-alphabetical columns
            if (!(DataType.valueOf(filterType)).isSupportsFilter()) {
                return (false);
            }

        } catch (java.lang.IllegalArgumentException e) {
            return (false);
        }

        //second field not formatted
        if (!(sepFilter[1].charAt(0) == '"' && sepFilter[1].charAt(sepFilter[1].length() - 1) == '"')) {
            return (false);
        }

        return (true);
    }

    public ArrayList<String> getFilters() {
        ArrayList<String> filters = new ArrayList();
        String input = this.getInputField().getText();
        Pattern pat = Pattern.compile(".*?:\".*?\"");
        Matcher mat = pat.matcher(input);

        while (mat.find()) {
            filters.add(mat.group().trim().replaceAll("\\\\", ""));
        }

        return (filters);
    }

}
