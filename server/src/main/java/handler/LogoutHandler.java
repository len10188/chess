package handler;

import com.google.gson.Gson;
import io.javalin.http.Handler;
import request.LogoutRequest;
import result.LogoutResult;
import service.LogoutService;
import service.ServiceException;

import static handler.HandlerUtils.sendError;

public class LogoutHandler {
    private LogoutService logoutService;
    private final Gson gson = new Gson();

    public LogoutHandler(LogoutService logoutService){
        this.logoutService = logoutService;
    }

    // DELETE /session (logout)
    public Handler logoutUser = ctx -> {
        try {
            // get token from header
            String authToken = ctx.header("Authorization");
            LogoutRequest request = new LogoutRequest(authToken);

            // Call service
            LogoutResult result = logoutService.logout(request);

            // respond successful
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

}
