package ui;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.CreateGameResult;
import result.ListGamesResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

public class ServerFacade {
    String serverUrl;
    String authToken;
    private final Gson gson = new Gson();

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public String register(String username, String password, String email) throws IOException, URISyntaxException {
        var path = "/user";
        RegisterRequest request = new RegisterRequest(username, password, email);
        var response = this.makeRequest("POST", path, request, AuthData.class);
        if (response == null) {
            return null;
        }
        this.authToken = response.authToken();
        return authToken;
    }

    public String login(String username, String password) throws IOException, URISyntaxException {
        String path = "/session";
        LoginRequest request = new LoginRequest(username, password);
        var response = this.makeRequest("POST", path, request, AuthData.class);
        if (response == null) {
            return null;
        }
        this.authToken = response.authToken();
        return authToken;
    }

    public void logout() throws IOException, URISyntaxException {
        if (authToken == null){
            throw new RuntimeException("You are not logged in.");
        }
        String path = "/session";
        this.makeRequest("DELETE", path, null, null);
        this.authToken = null;
    }

    public String createGame(String gameName) throws IOException, URISyntaxException {
        String path = "/game";
        CreateGameRequest request = new CreateGameRequest(gameName, authToken);
        var result = this.makeRequest("POST", path, request, CreateGameResult.class);
        if (result == null) return null;
        if (result.gameID() < 1) {
            return null; // create failed.
        } else {
            return gameName;
        }
    }

    public Collection<GameData> listGames() throws IOException, URISyntaxException {
        String path = "/game";
        var response = this.makeRequest("GET", path, null, ListGamesResult.class);
        if (response != null) {
            return response.games();
        }
        return null;
    }

    public boolean joinGame (String playerColor, int gameID) throws IOException, URISyntaxException {
        String path = "/game";
        JoinGameRequest request = new JoinGameRequest(null, playerColor, gameID);
        var result = this.makeRequest("PUT", path, request, null);
        return result != null ;
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws IOException, URISyntaxException {
        URL url = (new URI(serverUrl + path)).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setDoOutput(request != null);

        if (authToken != null){
            connection.setRequestProperty("authorization", authToken);
        }
        if (request != null) {
            connection.addRequestProperty("Content-Type", "application/json");
            String json = gson.toJson(request);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(json.getBytes());
            }
        }
        connection.connect();
        int status = connection.getResponseCode();


        if (status < 200 || status >= 300){
            connection.disconnect();
            return null;
        }

        if (responseClass == null) {
            connection.disconnect();
            //noinspection unchecked
            return (T) Boolean.TRUE;
        }

        try (InputStream bodyStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(bodyStream)) {
            return gson.fromJson(reader, responseClass);
        } finally {
            connection.disconnect();
        }
    }
    public void clear() throws IOException, URISyntaxException {
        String path = "/db";
        this.makeRequest("DELETE", path, null, null);
    }
}
