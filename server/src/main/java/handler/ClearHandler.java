package handler;

import com.google.gson.Gson;
import io.javalin.http.Handler;
import service.ClearService;

import java.util.Map;

public class ClearHandler {
    private ClearService clearService;
    private final Gson gson = new Gson();

    public ClearHandler(ClearService clearService){
        this.clearService = clearService;
    }

    // DELETE /db (clear database)
    public Handler clearAll = ctx -> {
        try{
            clearService.clearData();

            // empty success
            Map<String, Object> result = Map.of();

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        } catch (Exception e) {
            Map<String, String> error = Map.of("message", "Error: " + e.getMessage());
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(error));
        }
    };
}
