package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import request.CreateGameRequest;
import result.CreateGameResult;
import service.CreateGameService;
import service.ServiceException;

import java.util.Map;

public class CreateGameHandler {
    private CreateGameService createGameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(CreateGameService createGameService) {
        this.createGameService = createGameService;
    }

    public Handler createGame = ctx -> {
        try{
            String authToken = ctx.header("authorization");
            CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);

            CreateGameResult result = createGameService.createGame(authToken, request.gameName());

            // success
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));

        } catch (ServiceException.BadRequestException e) {
            sendError(ctx, 400, "Error: bad request");
        } catch (ServiceException.UnauthorizedException e) {
            sendError(ctx, 401, "Error: unauthorized");
        } catch (Exception e) {
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
