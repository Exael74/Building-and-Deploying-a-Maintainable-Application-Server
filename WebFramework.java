package co.edu.escuelaing.webframework;

import java.util.HashMap;
import java.util.Map;

public class webframework {
    static Map<String, WebService> webServices = new HashMap<>();

    public static void get(String route, WebService ws) {
        webServices.put(route, ws);
    }

    public void invoke(String route) {
        WebService ws = webServices.get(route);
        ws.call();
    }

    public static void start() {

    }
}