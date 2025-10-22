package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.JoinGameRequest;
import result.JoinGameResult;

import static org.junit.jupiter.api.Assertions.*;

public class JoinGameServiceTest {
    private MemoryAuthDAO authDAO;
    private  MemoryGameDAO gameDAO;
    private JoinGameService joinGameService;

    @BeforeEach
    void setup() {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        joinGameService = new JoinGameService(authDAO, gameDAO);
    }

    @Test
    void joinGameSuccessful() throws Exception {

        AuthData auth = authDAO.createAuth("Player1");
        GameData game = gameDAO.createGame("TestGame");

        JoinGameRequest request = new JoinGameRequest(auth.authToken(), "white", game.gameID());

        JoinGameResult result = joinGameService.joinGame(request);

        // Assert
        assertNotNull(result, "Result shouldn't be null");
        GameData updatedGame = gameDAO.getGame(game.gameID());
        assertEquals("Player1", updatedGame.whiteUsername(), "White username should be updated");
        assertNull(updatedGame.blackUsername(), "Black username should still be null");
    }

    @Test
    void joinGameAlreadyTaken() throws Exception {

        AuthData auth1 = authDAO.createAuth("Player1");
        AuthData auth2 = authDAO.createAuth("Player2");
        GameData game = gameDAO.createGame("TestGame");

        // player1 is white
        JoinGameRequest request1 = new JoinGameRequest(auth1.authToken(), "white", game.gameID());
        joinGameService.joinGame(request1);

        // player2 tries to join as white
        JoinGameRequest request2 = new JoinGameRequest(auth2.authToken(), "white", game.gameID());

        // assert
        assertThrows(ServiceException.AlreadyTakenException.class, () -> {
            joinGameService.joinGame(request2);
        });
    }
}
