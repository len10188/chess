package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.ListGamesRequest;
import result.ListGamesResult;


import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class ListGamesServiceTest {
    private AuthDAO authDAO;
    private ListGamesService listGamesService;

    @BeforeEach
    void setup() throws SQLException, DataAccessException {
        DAOSwitch.useMemory();

        authDAO = DAOSwitch.setAuthDAO();
        GameDAO gameDAO = DAOSwitch.setGameDAO();
        listGamesService = new ListGamesService(authDAO, gameDAO);

        // load games
        gameDAO.createGame("Game 1");
        gameDAO.createGame("Game 2");
    }

    @Test
    void listGamesSuccessfulTest() throws Exception {
        AuthData auth = authDAO.createAuth("ExistingUser");
        ListGamesRequest request = new ListGamesRequest(auth.authToken());

        ListGamesResult result = listGamesService.listGames(request);

        // assert
        assertNotNull(result);
        assertNotNull(result.games());
        assertEquals(2, result.games().size(), "There should be exactly 2 games returned");
    }
    @Test
    void listGamesUnauthorized() {
        // Arrange
        ListGamesRequest badRequest = new ListGamesRequest("invalid_token");

        // Act + Assert
        assertThrows(ServiceException.UnauthorizedException.class, () -> listGamesService.listGames(badRequest));
    }

}
