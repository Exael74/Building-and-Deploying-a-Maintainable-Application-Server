package edu.eci.tdse.webframework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Router {

    private final Map<String, GetLambda> getRoutes = new ConcurrentHashMap<>();
    private final Map<String, GetLambda> postRoutes = new ConcurrentHashMap<>();

    public void addGet(String path, GetLambda handler) {
        getRoutes.put(path, handler);
    }

    public void addPost(String path, GetLambda handler) {
        postRoutes.put(path, handler);
    }

    public GetLambda getRoute(String method, String path) {
        if ("POST".equalsIgnoreCase(method)) {
            return postRoutes.get(path);
        }
        return getRoutes.get(path);
    }
}
