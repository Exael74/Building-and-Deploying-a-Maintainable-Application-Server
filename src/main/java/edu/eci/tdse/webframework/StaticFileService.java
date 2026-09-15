package edu.eci.tdse.webframework;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StaticFileService {

    private final String staticFilesPath;

    public StaticFileService(String staticFilesPath) {
        this.staticFilesPath = staticFilesPath.startsWith("/") ? staticFilesPath.substring(1) : staticFilesPath;
    }

    public byte[] getFileContent(String path) {
        if (path.equals("/")) {
            path = "/index.html";
        }

        String resourcePath = "/" + staticFilesPath + path;
        try (InputStream is = StaticFileService.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }
            return is.readAllBytes();
        } catch (Exception e) {
            return null;
        }
    }

    public String getContentType(String path) {
        if (path.equals("/")) {
            path = "/index.html";
        }
        if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".json")) return "application/json; charset=utf-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "text/plain; charset=utf-8";
    }
}
