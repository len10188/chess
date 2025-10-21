package server;

import io.javalin.*;
import handler.UserHandler;
import org.eclipse.jetty.server.Authentication;
import service.UserService;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDOA;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        // Create DAO
        UserDAO userDAO = new MemoryUserDOA();
        AuthDAO authDAO = new MemoryAuthDAO();

        // Create services
        UserService userService = new UserService(userDAO, authDAO);

        // Create handler
        UserHandler userHandler = new UserHandler(userService);

        // routes
        javalin.post("/user", userHandler.registerUser);



    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
