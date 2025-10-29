package dataaccess;

import com.google.gson.Gson;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.*;

public class SQLUserDAO implements UserDAO{

    private final Gson gson = new Gson();

    private final String[] createStatement = {
            """
            CREATE TABLE IF NOT EXISTS users (
            username VARCHAR(255) NOT NULL,
            password VARCHAR(255) NOT NULL,
            email VARCHAR(255),
            PRIMARY KEY (username),
            INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
"""
    };

    public SQLUserDAO() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatement) {
                try (var preparedStatement = conn.prepareStatement(statement)){
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to configure user table", e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        // hash password
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
            var statement = conn.prepareStatement(sql)) {

            statement.setString(1, user.username());
            statement.setString(2, hashedPassword);
            statement.setString(3, user.email());
            statement.executeUpdate();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DataAccessException("Username already exists", e);
            }
            throw new DataAccessException("Error create user: " + e.getMessage(), e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {

            statement.setString(1, username);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String password = resultSet.getString("password");
                    String email = resultSet.getString("email");
                    return new UserData(username, password, email); // password is hashed.
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyUser(String username, String providedPassword) throws DataAccessException {
        UserData user = getUser(username);
        if (user == null) {
            return false; // no user found
        }
        return BCrypt.checkpw(providedPassword, user.password()); // check provided password against hashed password.
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "TRUNCATE TABLE users";
        try (Connection conn = DatabaseManager.getConnection();
            var statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users: " + e.getMessage());
        }
    }
}
