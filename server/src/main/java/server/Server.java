package server;

import handler.LoginHandler;
import io.javalin.*;
import handler.UserHandler;
import org.eclipse.jetty.server.Authentication;
import service.LoginService;
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
        LoginService loginService = new LoginService(userDAO, authDAO);

        // Create handler
        UserHandler userHandler = new UserHandler(userService);
        LoginHandler loginHandler = new LoginHandler(loginService);

        // routes
        javalin.post("/user", userHandler.registerUser);
        javalin.post("/session", loginHandler.loginUser);



    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
