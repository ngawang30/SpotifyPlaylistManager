package music.spotifyplaylistmanager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.swing.SwingWorker;
import org.json.JSONObject;
import org.json.JSONArray;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.Locale;
import java.net.URLEncoder;
import org.openqa.selenium.NoSuchWindowException;
import javax.swing.JProgressBar;
import java.util.Iterator;

public class APIHandler {

    public static JSONArray getPlaylistTracks(String playlistID) {
        int offSet = 0;
        JSONArray allTracks = new JSONArray();
        String response = APIHandler.getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?limit=50");
        JSONObject responseJSON = new JSONObject(response);
        int size = responseJSON.getInt("total");

        ProgressBarDialog pbd = new ProgressBarDialog("Loading Songs From Spotify", new JProgressBar(0, size));

        while (offSet < size) {
            response = APIHandler.getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?limit=50&offset=" + offSet);
            responseJSON = new JSONObject(response);
            allTracks.putAll(responseJSON.getJSONArray("items"));

            offSet += 50;
            pbd.setValue(offSet);
        }

        return (allTracks);
    }

    public static JSONArray generateCustomJSON(String playlistID) {
        JSONArray spotifyPlaylist = getPlaylistTracks(playlistID);
        JSONArray cache = new JSONArray(PlaylistManager.readFromCache());
        JSONArray customArray = new JSONArray();
        int counter = 0;
        ProgressBarDialog pbd = new ProgressBarDialog("Generating Track Information", new JProgressBar(0, spotifyPlaylist.length()));

        for (int i = 0; i < spotifyPlaylist.length(); i++) {
            JSONObject currentTrack = spotifyPlaylist.getJSONObject(i).getJSONObject("track");

            boolean inCache = false;
            Iterator it = cache.iterator();

            while (it.hasNext() && !inCache) {
                JSONObject comTrack = (JSONObject) it.next();
                //TrackName + Artist = key
                String comOne = comTrack.getString("trackName") + comTrack.getString("trackArtist");
                String comTwo = currentTrack.getString("name") + currentTrack.getJSONArray("artists").getJSONObject(0).getString("name");
                if (comOne.equals(comTwo)) {
                    inCache = true;
                    customArray.put(counter++, comTrack);
                }
            }

            if (!inCache && !isNullTrack(currentTrack)) {
                currentTrack = refineTrack(currentTrack);
                customArray.put(counter++, currentTrack);
            }

            pbd.incrementValue();
        }

        return (customArray);
    }

    public static boolean isNullTrack(JSONObject track) {
        if (track.getJSONObject("album").getString("release_date").equals("0000")) {
            return (true);
        } else {
            return (false);
        }
    }

    public static JSONObject refineTrack(JSONObject unrefinedTrack) {
        System.out.println(unrefinedTrack);
        JSONObject refinedTrack = parseSpotifyInfo(unrefinedTrack);
        parseMusicBrainzInfo(refinedTrack);
        return (refinedTrack);
    }

    public static JSONObject parseSpotifyInfo(JSONObject currentTrack) {
        JSONObject newJSON = new JSONObject();

        String trackCoverURL = currentTrack.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
        newJSON.put("trackCoverURL", trackCoverURL);

        String trackName = currentTrack.getString("name");
        newJSON.put("trackName", trackName);

        String trackArtist = currentTrack.getJSONArray("artists").getJSONObject(0).getString("name");
        newJSON.put("trackArtist", trackArtist);

        String album = currentTrack.getJSONObject("album").getString("name");
        newJSON.put("album", album);

        String releasedDate = currentTrack.getJSONObject("album").getString("release_date");
        newJSON.put("releasedDate", releasedDate);

        int durationInMs = currentTrack.getInt("duration_ms");
        int durationInSeconds = (durationInMs / 1000);
        String durationInSecondsString = String.valueOf(durationInSeconds);
        newJSON.put("durationInSeconds", durationInSecondsString);

        int duration = currentTrack.getInt("duration_ms");
        int min = (duration / 1000 / 60);
        int sec = (duration / 1000 % 60);
        String durationString = String.format("%02d:%02d", min, sec);
        newJSON.put("duration", durationString);

        int popularity = currentTrack.getInt("popularity");
        newJSON.put("popularity", String.valueOf(popularity));

        String artistID = currentTrack.getJSONArray("artists").getJSONObject(0).getString("id");
        newJSON.put("artistID", artistID);

        String explicit = String.valueOf(currentTrack.getBoolean("explicit"));
        newJSON.put("explicit", explicit);

        String isrc = currentTrack.getJSONObject("external_ids").getString("isrc");
        newJSON.put("isrc", isrc);

        String id = currentTrack.getString("id");
        newJSON.put("id", id);
        return (newJSON);
    }

    public static void parseMusicBrainzInfo(JSONObject currentTrack) {
        int rate = 1500;
        JSONObject backupTrack = new JSONObject(currentTrack.toString());

        try {
            //Request 1

            String query = "query=" + URLEncoder.encode("ANDrecording:\"" + (currentTrack.getString("trackName") + "\"ANDartist:\"" + currentTrack.getString("trackArtist") + "\"ANDrelease:\"" + currentTrack.getString("album")) + "\"", "UTF-8");

            HttpRequest musicBrainzRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://musicbrainz.org/ws/2/recording?" + query))
                    .headers("Accept", "application/json", "User-Agent", "SpotifyPlaylistManager ( ngawang30@gmail.com )")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(musicBrainzRequest, HttpResponse.BodyHandlers.ofString());
            JSONObject newBrainz = new JSONObject(response.body());

            String brainzRecordingID = newBrainz.getJSONArray("recordings").getJSONObject(0).getString("id");
            currentTrack.put("brainzRecordingID", brainzRecordingID);

            String brainzArtistID = newBrainz.getJSONArray("recordings").getJSONObject(0).getJSONArray("artist-credit").getJSONObject(0).getJSONObject("artist").getString("id");
            currentTrack.put("brainzArtistID", brainzArtistID);

            //API LimitCheck
            Thread.sleep(rate);

            //Request 2
            musicBrainzRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://musicbrainz.org/ws/2/artist/" + brainzArtistID))
                    .headers("Accept", "application/json", "User-Agent", "SpotifyPlaylistManager ( ngawang30@gmail.com )")
                    .GET()
                    .build();

            response = HttpClient.newHttpClient().send(musicBrainzRequest, HttpResponse.BodyHandlers.ofString());
            newBrainz = new JSONObject(response.body());

            String artistType = newBrainz.optString("type", "null");
            currentTrack.put("artistType", artistType);

            String artistCountry = newBrainz.isNull("area") ? "null" : newBrainz.getJSONObject("area").getString("name");
            currentTrack.put("artistCountry", artistCountry);

            String artistGender = newBrainz.isNull("gender") ? "null" : newBrainz.getString("gender");
            currentTrack.put("artistGender", artistGender);

            String isDead = newBrainz.isNull("life-span") ? "null" : String.valueOf(newBrainz.getJSONObject("life-span").getBoolean("ended"));
            currentTrack.put("isDead", isDead);

            String subArea = newBrainz.isNull("begin_area") ? "null" : newBrainz.getJSONObject("begin_area").getString("name");
            currentTrack.put("subArea", subArea);

            //API LimitCheck
            Thread.sleep(rate);

            //request 3
            musicBrainzRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://musicbrainz.org/ws/2/recording/" + brainzRecordingID + "?inc=releases"))
                    .headers("Accept", "application/json", "User-Agent", "SpotifyPlaylistManager ( ngawang30@gmail.com )")
                    .GET()
                    .build();

            response = HttpClient.newHttpClient().send(musicBrainzRequest, HttpResponse.BodyHandlers.ofString());
            newBrainz = new JSONObject(response.body());

            JSONObject languageRoot = newBrainz.getJSONArray("releases").optJSONObject(0);
            String language;
            if (languageRoot != null) {
                language = languageRoot.isNull("language") ? "null" : (new Locale(newBrainz.getJSONArray("releases").getJSONObject(0).getJSONObject("text-representation").getString("language"))).getDisplayLanguage();
            } else {
                language = "null";
            }
            currentTrack.put("language", language);

            //API LimitCheck
            Thread.sleep(rate);

            PlaylistManager.writeToCache(currentTrack.toString());
        } catch (Exception e) {
            e.printStackTrace();

            try {
                Thread.sleep(rate * 8);
                parseMusicBrainzInfo(backupTrack);
                PlaylistManager.writeToCache(currentTrack.toString());
            } catch (Exception ee) {
                ee.printStackTrace();
            }

        }
    }

    //Spotify
    public static String getRequestResponse(String url) {
        String response = "error";

        try {
            HttpRequest generalRequest = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .headers("Authorization", "Bearer " + PlaylistManager.getAuthorizedToken())
                    .GET()
                    .build();

            HttpResponse<String> responseBody = HttpClient.newHttpClient().send(generalRequest, HttpResponse.BodyHandlers.ofString());
            response = responseBody.body();

        } catch (Exception e) {}

        return (response);
    }

    public static String getAuthorizationCode(String codeChallenge) {
        String site = "https://accounts.spotify.com/authorize?"
                + "client_id=c592a3c9e9b34be59a79bfe0b98aa6a1"
                + "&redirect_uri=http://localhost/"
                + "&response_type=code"
                + "&scope=user-read-email,user-read-private,playlist-modify-public,playlist-modify-private,playlist-read-private,user-read-playback-state,user-modify-playback-state,user-read-currently-playing"
                + "&code_challenge_method=S256"
                + "&code_challenge=" + codeChallenge;
        WebDriver driver = new ChromeDriver();
        driver.get(site);
        String authorizationCode;

        SwingWorker sw = new SwingWorker() {
            @Override
            protected String doInBackground() {
                try {
                    while (driver.getTitle() != null) {
                    }
                } catch (NoSuchWindowException e) {
                    driver.quit();
                }

                return ("");
            }

            @Override
            protected void done() {
            }

        };

        sw.execute();

        new WebDriverWait(driver, Duration.ofMinutes(2)).until(ExpectedConditions.urlContains("localhost/?code"));

        authorizationCode = driver.getCurrentUrl();
        authorizationCode = authorizationCode.substring(authorizationCode.indexOf("=") + 1);
        driver.quit();

        return (authorizationCode);
    }

    public static String getAuthorizedToken() {
        String codeVerifier = PlaylistManager.codeVerifier();
        String codeChallenge = PlaylistManager.codeChallenge(codeVerifier);
        String clientID = "c592a3c9e9b34be59a79bfe0b98aa6a1";
        HttpResponse<String> response = null;
        String authorizationCode = getAuthorizationCode(codeChallenge);

        byte[] requestBody = new String("grant_type=authorization_code&code=" + authorizationCode + "&redirect_uri=http://localhost/&client_id=" + clientID + "&code_verifier=" + codeVerifier).getBytes();

        try {
            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://accounts.spotify.com/api/token"))
                    .headers("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .build();

            response = HttpClient.newHttpClient().send(tokenRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
        }

        String token = new JSONObject(response.body()).getString("access_token");

        return (token);
    }
}
