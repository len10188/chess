package handler;

import com.google.gson.Gson;
import io.javalin.http.Handler;
import request.ListGamesRequest;
import result.ListGamesResult;
import service.ListGamesService;
import service.ServiceException;

import static handler.HandlerUtils.sendError;

public class ListGamesHandler {
    private ListGamesService listGamesService;
    private final Gson gson = new Gson();

    public ListGamesHandler(ListGamesService listGamesService){
        this.listGamesService = listGamesService;
    }

    // Get /game (list all games)
    public Handler listGames = ctx -> {
        try {
            // Get authToken from header
            String authToken = ctx.header("Authorization");
            ListGamesRequest request = new ListGamesRequest(authToken);

            // Call service
            ListGamesResult result = listGamesService.listGames(request);

            // respond successful
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));

        } catch (ServiceException.UnauthorizedException e) {
            sendError(ctx,401, "Error: unauthorized");
        } catch (Exception e){
            sendError(ctx, 500, "Error: " + e.getMessage());
        }
    };

}
