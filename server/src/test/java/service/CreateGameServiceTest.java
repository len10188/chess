package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import result.CreateGameResult;


import static org.junit.jupiter.api.Assertions.*;

public class CreateGameServiceTest {

    private MemoryAuthDAO authDAO;
    private MemoryGameDAO gameDAO;
    private CreateGameService createGameService;

    @BeforeEach
    void setup() {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        createGameService = new CreateGameService(authDAO, gameDAO);
    }

    @Test
    void createGameSuccessful() throws Exception {
        AuthData auth = authDAO.createAuth("ExistingUser");
        String authToken = auth.authToken();
        String gameName = "TestGame";

        CreateGameResult result = createGameService.createGame(authToken, gameName);

        // assert

        assertNotNull(result);
        assertTrue(result.gameID() > 0, "Game ID should be greater than zero");
        assertEquals(1, gameDAO.listGames().size(), "Exactly one game should exist");
    }

    @Test
    void createGameUnauthorized() {
        // Arrange: use invalid auth token
        String invalidToken = "badToken";
        String gameName = "TestGame";

        // Act & Assert
        assertThrows(ServiceException.UnauthorizedException.class, () -> createGameService.createGame(invalidToken, gameName));
    }
}
