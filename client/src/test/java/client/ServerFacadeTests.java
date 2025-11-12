package client;

import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;
import model.GameData;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private ServerFacade facade;
    private static String baseUrl;


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        baseUrl = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setup() throws Exception {
        facade = new ServerFacade(baseUrl);
        facade.clear();
    }

    // REGISTER
    @Test
    public void registerPositive() throws Exception {
        String token = facade.register("laura", "password", "email@byu.edu");
        assertNotNull(token, "register should return a token");
    }

    @Test
    public void registerNegative() throws Exception {
        facade.register("laura", "password", "email@byu.edu");
        String token2 = facade.register("laura", "password", "email2@byu.edu");
        assertNull(token2, "registering duplicate should produce null token (fail).");
    }

    // LOGIN
    @Test
    public void loginPositive() throws Exception {
        facade.register("loginTest", "password", "email@byu.edu");
        String token = facade.login("loginTest", "password");
        assertNotNull(token, "login should return a token upon success.");
    }

    @Test
    public void loginNegative() throws Exception {
        facade.register("loginTest", "password", "email@byu.edu");
        String token = facade.login("loginTest", "wrongPass");
        assertNull(token, "login should return null if login fail.");
    }

    // LOGOUT
    @Test
    public void logoutPositive() throws Exception {
        facade.register("logoutTest", "password", "email@byu.edu");
        assertDoesNotThrow(facade::logout, "logout after login or register should not throw");
    }

    @Test
    public void logoutNegative_notLoggedIn() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> facade.logout(),
                "logout without login should throw");
        assertTrue(ex.getMessage().toLowerCase().contains("not logged in"));
    }

    // CREATE GAME
    @Test
    public void createGamePositive() throws Exception {
        facade.register("createGame", "password", "test@email.com");
        String gameName = facade.createGame("game1");
        assertEquals("game1", gameName, "create game should return gameData not null upon success");
    }

    @Test
    public void createGameNegative() throws Exception {
        String gameName = facade.createGame("notLoggedIn");
        assertNull(gameName, "create game while not logged in should return null");
    }

    // LIST GAME
    @Test
    void listGamesPositive() throws Exception {
        facade.register("listGames", "password", "test@email.com");
        facade.createGame("A");
        facade.createGame("B");
        Collection<GameData> games = facade.listGames();
        assertNotNull(games, "listGames should not be null");
        assertTrue(games.size() >= 2, "should list at least the two created games");
    }

    @Test
    void listGamesNegative_notLoggedIn() throws Exception {
        Collection<GameData> games = facade.listGames();
        // Your facade returns null if response was null / error
        assertNull(games, "listGames when not logged in should return null");
    }


    //JOIN GAME
    @Test
    void joinGamePositive() throws Exception {
        String user1 = "player1";
        facade.register(user1, "password", "test@email.com");

        String createdName = facade.createGame("TESTGAME");
        assertNotNull(createdName, "game should be made");

        // find the gameID by name
        var games = facade.listGames();
        assertNotNull(games, "listGames should not be null after login");
        var game = games.stream()
                .filter(g -> "TESTGAME".equals(g.gameName()))
                .findFirst().orElseThrow();

        // join as white
        assertTrue(facade.joinGame("white", game.gameID()),
                "joining an existing game as white should succeed");

        // verify state
        var after = facade.listGames();
        var found = after.stream().filter(g -> g.gameID() == game.gameID()).findFirst().orElse(null);
        assertNotNull(found, "created game should still exist");
        assertEquals(user1, found.whiteUsername(), "white seat should be the creator");
    }

    @Test
    void joinGameNegative_alreadyTaken() throws Exception {
        // user1 takes white
        ServerFacade facade1 = new ServerFacade(baseUrl);
        facade1.register("player1", "password", "test@email.com");
        GameData game = facade1.createGame("TestGame");
        facade1.joinGame("white", game.gameID());

        // user2 tries to also take white (server should reject)
        ServerFacade facade2 = new ServerFacade(baseUrl);
        facade2.register("player2", "password", "test@email.com");
        // Your facade's joinGame returns void and swallows error bodies,
        // so we validate state instead of expecting an exception.
        facade2.joinGame("white", game.gameID());

        // Check that white is still player1
        Collection<GameData> games = facade2.listGames();
        GameData found = games.stream().filter(g -> g.gameID() == game.gameID()).findFirst().orElse(null);
        assertNotNull(found);
        assertEquals("player1", found.whiteUsername(),
                "white seat should remain with the first player after second attempt");
    }
    // CLEAR

}

