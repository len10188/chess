package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO{
    private final Map<String, AuthData> authTokens = new HashMap<>();

    @Override
    public AuthData createAuth(String username) {
        // Make new random token
        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(token, username);

        // Store authData in memory
        authTokens.put(token, authData);

        return authData;
    }

    @Override
    public AuthData getAuth(String token) {
        return authTokens.get(token);
    }

    @Override
    public void deleteAuth(String token) {
        authTokens.remove(token);
    }

    @Override
    public void clear() {
        authTokens.clear();
    }
}
