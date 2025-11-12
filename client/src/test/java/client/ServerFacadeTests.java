package client;

import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;
import model.GameData;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;


public class ServerFacadeTests {

    private static Server server;
    private ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setup() throws Exception {
        clearDb();
        facade = new ServerFacade(baseUrl);
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

}
