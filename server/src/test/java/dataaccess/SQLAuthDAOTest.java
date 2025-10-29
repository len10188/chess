package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

public class SQLAuthDAOTest {
    private SQLAuthDAO authDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        DAOSwitch.useDatabase();
        authDAO = new SQLAuthDAO();
        authDAO.clear();
    }
}