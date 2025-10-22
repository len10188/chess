package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import request.LoginRequest;
import result.LoginResult;
import service.LoginService;
import service.ServiceException;

import java.util.Map;

public class LoginHandler {
    private final LoginService loginService;
    private final Gson gson = new Gson();

    public LoginHandler(LoginService loginService) {
        this.loginService = loginService;
    }

    // Post /session (login)
    public Handler loginUser = ctx -> {
        try {
            // parse
            LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);

            // call service
            LoginResult result = loginService.login(request);

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        } catch (ServiceException.UnauthorizedException e) {
            sendError(ctx,401, "Error: unauthorized");
        } catch (ServiceException.BadRequestException e){
            sendError(ctx, 400, "Error: bad rquest");
        } catch (Exception e){
            sendError(ctx, 500, "Error: " + e.getMessage());
        }

    };

    private void sendError(Context ctx, int statusCode, String message){
        var errorBody = Map.of("message", message);
        ctx.status(statusCode);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(errorBody));
    }
}
