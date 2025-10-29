package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    @Test
    void clearDataSuccessful() throws DataAccessException {
        DAOSwitch.useMemory();

        MemoryUserDOA userDOA = new MemoryUserDOA();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

        // populate DAOs
        userDOA.createUser(new UserData("ExistingUser", "password", "email"));
        AuthData auth = authDAO.createAuth("ExistingUser");
        gameDAO.createGame("ChessGame");

        ClearService clearService = new ClearService(userDOA, gameDAO, authDAO);

        clearService.clearData();

        // assert
        assertNull(userDOA.getUser("ExistingUser"));
        assertNull(authDAO.getAuth(auth.authToken()));
        assertNull(gameDAO.getGame(1));}

}
