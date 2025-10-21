package handler;

import request.RegisterRequest;
import result.RegisterResult;
import service.UserService;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService){
        this.userService = userService;
    }

    // POST /user (register)
    public Handler registerUser = ctx -> {
        try {
            RegisterRequest request = ctx.bodyAsClass(RegisterRequest.class);
            RegisterResult result = userService.register(request);
            ctx.status(200).json(result);
        } catch (UserService.AlreadyTakenException e) {
            ctx.status(403).json(new RegisterResult("Error: username already taken"));
        } catch (UserService.BadRequestException e){
            ctx.status(400).json(new RegisterResult("Error: bad request"));
        } catch (Exception e){
            ctx.status(500).json(new RegisterResult("Error: " + e.getMessage()));
        }
    };
}
