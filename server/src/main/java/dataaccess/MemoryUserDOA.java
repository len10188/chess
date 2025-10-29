package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDOA implements  UserDAO{
    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public boolean verifyUser(String username, String providedPassword) throws DataAccessException {
        UserData user = getUser(username);
        if (user == null) {
            return false;
        }
        return providedPassword.equals(user.password());
    }

    @Override
    public void clear() {
        users.clear();
    }
}
