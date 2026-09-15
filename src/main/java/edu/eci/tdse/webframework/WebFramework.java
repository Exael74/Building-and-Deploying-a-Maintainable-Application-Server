package edu.eci.tdse.webframework;

public class WebFramework {

    private static int port = 8080;
    private static String staticFilesLocation = "webroot";
    private static final Router router = new Router();
    private static HttpServer server;

    public static void port(int p) {
        port = p;
    }

    public static void staticfiles(String path) {
        staticFilesLocation = path;
    }

    public static void get(String path, GetLambda handler) {
        router.addGet(path, handler);
    }

    public static void post(String path, GetLambda handler) {
        router.addPost(path, handler);
    }

    public static void start() {
        start(port);
    }

    public static void start(int p) {
        port = p;
        StaticFileService staticFileService = new StaticFileService(staticFilesLocation);
        server = new HttpServer(port, router, staticFileService);
        server.start();
    }

    public static void stop() {
        if (server != null) {
            server.stop();
        }
    }
}
