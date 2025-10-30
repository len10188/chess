package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTest {
    private SQLAuthDAO authDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        DAOSwitch.useDatabase();
        authDAO = new SQLAuthDAO();
        authDAO.clear();
    }

    @Test
    void createAuthPositive() throws DataAccessException {
        var result = authDAO.createAuth("test");

        assertNotNull(result, "Auth Data should not be null");
        assertEquals("test", result.username(), "Username should match");
        assertNotNull(result.authToken(), "Token should not be null");

        var fromDatabase = authDAO.getAuth(result.authToken());
        assertEquals("test", fromDatabase.username(), "stored auth username should match username");

    }

    @Test
    void createAuthDuplicateNegative() throws DataAccessException {
        var result1 = authDAO.createAuth("test");
        var result2 = authDAO.createAuth("test");

        assertNotEquals(result1.authToken(), result2.authToken(), "each created authToken should be unique");

    }

    @Test
    void getAuthPositive() throws DataAccessException {
        var original = authDAO.createAuth("test");

        var test = authDAO.getAuth(original.authToken());

        assertNotNull(original.authToken(), "authToken should not be null");
        assertEquals(original.authToken(), test.authToken(), "Auth tokens should match");
    }

    @Test
    void getAuthNegative() throws DataAccessException {
        AuthData auth = authDAO.getAuth("fake-token");
        assertNull(auth, "Nonexistent token returns null");
    }

    @Test
    void deleteAuthPositive() throws DataAccessException {
        AuthData auth = authDAO.createAuth("test");
        authDAO.deleteAuth(auth.authToken());
        assertNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    void deleteAuthNegative() {
        assertDoesNotThrow(() ->authDAO.deleteAuth("test"), "tyring to delete a non-existent auth token"+
                "is graceful");
    }

    @Test
    void clearPositive() throws DataAccessException {
        authDAO.createAuth("test1");
        authDAO.createAuth("test2");
        authDAO.clear();
        assertNull(authDAO.getAuth("test1"), "should be empty");
    }
}