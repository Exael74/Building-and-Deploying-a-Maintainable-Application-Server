package edu.eci.tdse.app;

import edu.eci.tdse.webframework.WebFramework;

public class Application {
    public static void main(String[] args) {
        // Externalized, deployment-specific configuration (never hard-coded)
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null && !portEnv.isBlank()) ? Integer.parseInt(portEnv) : 8080;

        String staticFilesPath = System.getenv().getOrDefault("STATIC_FILES_PATH", "webroot");
        String greetingPrefix = System.getenv().getOrDefault("GREETING_PREFIX", "Hello");
        String appEnv = System.getenv().getOrDefault("APP_ENV", "development");

        WebFramework.staticfiles(staticFilesPath);

        // Signatures follow exactly the (req, resp) -> String contract requested in the lab
        WebFramework.get("/hello", (req, resp) -> {
            String name = req.getValue("name");
            if (name == null || name.isBlank()) {
                name = "World";
            }
            return "{\"message\": \"" + greetingPrefix + ", " + name + "!\"}";
        });

        WebFramework.get("/pi", (req, resp) -> {
            return "{\"pi\": " + Math.PI + "}";
        });

        WebFramework.get("/api/status", (req, resp) -> {
            return "{\"status\": \"UP\", \"framework\": \"Sequential Custom HttpServer\", \"env\": \""
                    + appEnv + "\"}";
        });

        // Graceful shutdown: only reachable in development, never in production
        if ("development".equals(appEnv)) {
            WebFramework.get("/shutdown", (req, resp) -> {
                WebFramework.stop();
                return "{\"message\": \"Server will stop after this response.\"}";
            });
        }

        WebFramework.start(port);
    }
}
