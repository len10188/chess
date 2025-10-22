package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDOA;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.ClearService;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    private MemoryUserDOA userDOA;
    private MemoryAuthDAO authDAO;
    private MemoryGameDAO gameDAO;
    private ClearService clearService;

    @Test
    void ClearDataSuccessful() {

        userDOA = new MemoryUserDOA();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        // populate DAOs
        userDOA.createUser(new UserData("ExistingUser", "password", "email"));
        AuthData auth = authDAO.createAuth("ExistingUser");
        gameDAO.createGame("ChessGame");

        clearService = new ClearService(userDOA, gameDAO, authDAO);

        clearService.clearData();

        // assert
        assertNull(userDOA.getUser("ExistingUser"));
        assertNull(authDAO.getAuth(auth.authToken()));
        assertNull(gameDAO.getGame(1));}

}
