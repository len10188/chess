package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;

import java.util.Map;

public class HandlerUtils {

    private static final Gson gson = new Gson();

    private HandlerUtils() {

    }

    public static void sendError(Context ctx, int statusCode, String message){
        var errorBody = Map.of("message", message);
        ctx.status(statusCode);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(errorBody));
    }
}
