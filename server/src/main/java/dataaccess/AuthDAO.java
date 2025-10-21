package dataaccess;

import model.AuthData;

public interface AuthDAO {
    AuthData createAuth(String username);
    AuthData getAuth(String token);
    void deleteAuth(String token);
    void clear();
}
