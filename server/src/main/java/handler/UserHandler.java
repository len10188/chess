package handler;

import com.google.gson.Gson;
import request.RegisterRequest;
import result.RegisterResult;
import service.UserService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.serviceException;

import java.util.Map;

public class UserHandler {
    private UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService){
        this.userService = userService;

    }

    // POST /user (register)
    public Handler registerUser = ctx -> {
        try {
            // parse body
            RegisterRequest request = gson.fromJson(ctx.body(),(RegisterRequest.class));
            //Call service
            RegisterResult result = userService.register(request);
            // respond
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));

            // handler errors
        } catch (serviceException.AlreadyTakenException e) {
            sendError(ctx,403, "Error: username already taken");
        } catch (serviceException.BadRequestException e){
            sendError(ctx, 400, "Error: bad request");
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
