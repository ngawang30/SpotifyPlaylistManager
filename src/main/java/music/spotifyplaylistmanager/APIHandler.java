package music.spotifyplaylistmanager;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.swing.SwingWorker;
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
import java.util.Optional;

public class APIHandler {

    public static JsonArray getJsonPlaylistTracks(String playlistID) {
        int offSet = 0;
        JsonArray allTracks = new JsonArray();
        String response = APIHandler.getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?limit=50");
        JsonObject responseJSON = JsonParser.parseString(response).getAsJsonObject();
        int size = responseJSON.get("total").getAsInt();

        ProgressBarDialog pbd = new ProgressBarDialog("Loading Songs From Spotify", new JProgressBar(0, size));

        while (offSet < size) {
            response = APIHandler.getRequestResponse("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?limit=50&offset=" + offSet);
            responseJSON = JsonParser.parseString(response).getAsJsonObject();
            allTracks.addAll(responseJSON.getAsJsonArray("items"));

            offSet += 50;
            pbd.setValue(offSet);
        }

        return (allTracks);
    }

    public static JsonArray generateCustomJSON(String playlistID) {
        JsonArray spotifyPlaylist = getJsonPlaylistTracks(playlistID);
        JsonArray cache = JsonParser.parseString(PlaylistManager.readFromCache()).getAsJsonArray();
        JsonArray customArray = new JsonArray();
        int counter = 0;
        ProgressBarDialog pbd = new ProgressBarDialog("Generating Track Information", new JProgressBar(0, spotifyPlaylist.size()));

        for (int i = 0; i < spotifyPlaylist.size(); i++) {
            JsonObject currentTrack = spotifyPlaylist.get(i).getAsJsonObject().get("track").getAsJsonObject();

            boolean inCache = false;
            Iterator it = cache.iterator();

            while (it.hasNext() && !inCache) {
                JsonObject comTrack = (JsonObject) it.next();
                //TrackName + Artist = key
                String comOne = comTrack.get("trackName").getAsString() + comTrack.get("trackArtist").getAsString();
                String comTwo = currentTrack.get("name").getAsString() + currentTrack.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString();
                if (comOne.equals(comTwo)) {
                    inCache = true;
                    customArray.add(comTrack);
                }
            }

            if (!inCache && !isNullTrack(currentTrack)) {
                currentTrack = refineTrackJson(currentTrack);
                customArray.add(currentTrack);
            }

            pbd.incrementValue();
        }

        return (customArray);
    }

    public static boolean isNullTrack(JsonObject track) {
        if (track.getAsJsonObject("album").get("release_date").equals("0000")) {
            return (true);
        } else {
            return (false);
        }
    }

    public static JsonObject refineTrackJson(JsonObject unrefinedTrack) {
        System.out.println(unrefinedTrack);
        JsonObject refinedTrack = parseSpotifyInfo(unrefinedTrack);
        parseMusicBrainzInfo(refinedTrack);
        return (refinedTrack);
    }

    public static JsonObject parseSpotifyInfo(JsonObject currentTrack) {
        JsonObject newJSON = new JsonObject();

        String trackCoverURL = currentTrack.getAsJsonObject("album").getAsJsonArray("images").get(0).getAsJsonObject().get("url").getAsString();
        newJSON.add("trackCoverURL", new JsonPrimitive(trackCoverURL));

        System.out.println("123");

        String trackName = currentTrack.get("name").getAsString();
        newJSON.add("trackName", new JsonPrimitive(trackName));

        String trackArtist = currentTrack.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString();
        newJSON.add("trackArtist", new JsonPrimitive(trackArtist));

        String album = currentTrack.getAsJsonObject("album").get("name").getAsString();
        newJSON.add("album", new JsonPrimitive(album));

        String releasedDate = currentTrack.getAsJsonObject("album").get("release_date").getAsString();
        newJSON.add("releasedDate", new JsonPrimitive(releasedDate));

        int durationInMs = currentTrack.get("duration_ms").getAsInt();
        int durationInSeconds = (durationInMs / 1000);
        String durationInSecondsString = String.valueOf(durationInSeconds);
        newJSON.add("durationInSeconds", new JsonPrimitive(durationInSecondsString));

        int duration = currentTrack.get("duration_ms").getAsInt();
        int min = (duration / 1000 / 60);
        int sec = (duration / 1000 % 60);
        String durationString = String.format("%02d:%02d", min, sec);
        newJSON.add("duration", new JsonPrimitive(durationString));

        int popularity = currentTrack.get("popularity").getAsInt();
        newJSON.add("popularity", new JsonPrimitive(String.valueOf(popularity)));

        String artistID = currentTrack.getAsJsonArray("artists").get(0).getAsJsonObject().get("id").getAsString();
        newJSON.add("artistID", new JsonPrimitive(artistID));

        String explicit = String.valueOf(currentTrack.get("explicit"));
        newJSON.add("explicit", new JsonPrimitive(explicit));

        String uri = currentTrack.getAsJsonPrimitive("uri").getAsString();
        newJSON.add("uri", new JsonPrimitive(uri));

        String id = currentTrack.get("id").getAsString();
        newJSON.add("id", new JsonPrimitive(id));
        return (newJSON);
    }

    public static void parseMusicBrainzInfo(JsonObject currentTrack) {
        int rate = 1500;
        JsonObject backupTrack = JsonParser.parseString(currentTrack.toString()).getAsJsonObject();

        try {
            //Request 1

            String query = "query=" + URLEncoder.encode("ANDrecording:\"" + (currentTrack.get("trackName") + "\"ANDartist:\"" + currentTrack.get("trackArtist") + "\"ANDrelease:\"" + currentTrack.get("album")) + "\"", "UTF-8");

            HttpRequest musicBrainzRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://musicbrainz.org/ws/2/recording?" + query))
                    .headers("Accept", "application/json", "User-Agent", "SpotifyPlaylistManager ( ngawang30@gmail.com )")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(musicBrainzRequest, HttpResponse.BodyHandlers.ofString());
            Optional<JsonObject> newBrainz = Optional.of(JsonParser.parseString(response.body()).getAsJsonObject());

            newBrainz.map(data -> data.get("recordings"))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonArray().get(0))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonObject().get("id"))
                    .ifPresent(data -> currentTrack.add("brainzRecordingID", data.isJsonNull() ? JsonParser.parseString("-") : data));

            newBrainz.map(data -> data.get("recordings"))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonArray().get(0))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonObject().get("artist-credit"))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonArray().get(0))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonObject().get("artist"))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonObject().get("id"))
                    .ifPresent(data -> currentTrack.add("brainzArtistID", data.isJsonNull() ? JsonParser.parseString("-") : data));

            //API LimitCheck
            Thread.sleep(rate);

            //Request 2
            musicBrainzRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://musicbrainz.org/ws/2/artist/" + currentTrack.get("brainzArtistID").getAsString()))
                    .headers("Accept", "application/json", "User-Agent", "SpotifyPlaylistManager ( ngawang30@gmail.com )")
                    .GET()
                    .build();

            response = HttpClient.newHttpClient().send(musicBrainzRequest, HttpResponse.BodyHandlers.ofString());
            newBrainz = Optional.of(JsonParser.parseString(response.body()).getAsJsonObject());

            newBrainz.map(data -> data.get("type"))
                    .ifPresent(data -> currentTrack.add("artistType", data.isJsonNull() ? JsonParser.parseString("-") : data));

            newBrainz.map(data -> data.get("area"))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonObject().get("name"))
                    .ifPresent(data -> currentTrack.add("artistCountry", data.isJsonNull() ? JsonParser.parseString("-") : data));

            newBrainz.map(data -> data.get("gender"))
                    .ifPresent(data -> currentTrack.add("artistGender", data.isJsonNull() ? JsonParser.parseString("-") : data));

            newBrainz.map(data -> data.get("life-span"))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonObject().get("ended"))
                    .ifPresent(data -> currentTrack.add("isDead", data.isJsonNull() ? JsonParser.parseString("-") : data));

            newBrainz.map(data -> data.get("begin-area"))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonObject().get("name"))
                    .ifPresent(data -> currentTrack.add("subArea", data.isJsonNull() ? JsonParser.parseString("-") : data));

            //API LimitCheck
            Thread.sleep(rate);

            //request 3
            musicBrainzRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://musicbrainz.org/ws/2/recording/" + currentTrack.get("brainzRecordingID").getAsString() + "?inc=releases"))
                    .headers("Accept", "application/json", "User-Agent", "SpotifyPlaylistManager ( ngawang30@gmail.com )")
                    .GET()
                    .build();

            response = HttpClient.newHttpClient().send(musicBrainzRequest, HttpResponse.BodyHandlers.ofString());
            newBrainz = Optional.of(JsonParser.parseString(response.body()).getAsJsonObject());

            newBrainz.map(data -> data.get("releases"))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonArray().get(0))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonObject().get("text-representation"))
                    .map(data -> data.isJsonNull() ? JsonNull.INSTANCE : data.getAsJsonObject().get("language"))
                    .ifPresent(data -> currentTrack.add("language", data.isJsonNull() ? JsonParser.parseString("-") : JsonParser.parseString(new Locale(data.getAsString()).toString())));

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

        } catch (Exception e) {
            e.printStackTrace();
        }

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

        String token = JsonParser.parseString(response.body()).getAsJsonObject().get("access_token").getAsString();

        return (token);
    }
}
