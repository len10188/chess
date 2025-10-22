package handler;

import com.google.gson.Gson;
import io.javalin.http.Handler;
import request.JoinGameRequest;
import result.JoinGameResult;
import service.JoinGameService;
import service.ServiceException;

import static handler.HandlerUtils.sendError;

public class JoinGameHandler {
    private JoinGameService joinGameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(JoinGameService joinGameService){
        this.joinGameService = joinGameService;
    }

    public Handler joinGame = ctx -> {
        try {
            String authToken = ctx.header("authorization");
            JoinGameRequest body = gson.fromJson(ctx.body(), JoinGameRequest.class);
            JoinGameRequest request = new JoinGameRequest(authToken, body.playerColor(), body.gameID());

            JoinGameResult result = joinGameService.joinGame(request);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));

        } catch (ServiceException.UnauthorizedException e) {
            sendError(ctx,401, "Error: unauthorized");
        } catch (ServiceException.AlreadyTakenException e) {
            sendError(ctx, 403, "Error: already taken");
        } catch (ServiceException.BadRequestException e){
            sendError(ctx, 400, "Error: bad request");
        } catch (Exception e){
            sendError(ctx, 500, "Error: " + e.getMessage());
        }
    };
}
