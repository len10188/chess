package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


public class SQLUserDAOTest {
    private SQLUserDAO userDAO;

    UserData user = new UserData("test", "testPass", "email");

    @BeforeEach
    void setup() throws DataAccessException {
        DAOSwitch.useDatabase();
        userDAO = new SQLUserDAO();
        userDAO.clear();
    }

    @Test
    void createUserPositive() throws DataAccessException {
        userDAO.createUser(user);

        var fromDB = userDAO.getUser("test");
        assertNotNull(fromDB);
        assertEquals("test", fromDB.username());
    }

    @Test
    void createUserNegative() throws DataAccessException {
        userDAO.createUser(user);
        assertThrows(DataAccessException.class, () -> userDAO.createUser(user));
    }

    @Test
    void getUserPositive() throws DataAccessException {
        // set up
        userDAO.createUser(user);
        var result = userDAO.getUser("test");
        assertNotNull(result);
        assertEquals("test", result.username());
        assertEquals("email", result.email());
    }

    @Test
    void getUserNegative() throws DataAccessException {
        assertNull(userDAO.getUser("fakeUser"));
    }

    @Test
    void verifyUserPositive() throws DataAccessException {
        userDAO.createUser(user);

        boolean checkVerify = userDAO.verifyUser("test", "testPass");

        assertTrue(checkVerify);
    }

    @Test
    void verifyUserNegative() throws DataAccessException {
        userDAO.createUser(user);

        boolean checkVerify = userDAO.verifyUser("test", "wrongPass");

        assertFalse(checkVerify);
    }

    @Test
    void clearUsers() throws DataAccessException {
        userDAO.createUser(user);
        userDAO.clear();
        assertNull(userDAO.getUser("test"));
    }
}
