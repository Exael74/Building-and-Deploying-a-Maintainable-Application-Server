package edu.eci.tdse.webframework;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {

    private final int port;
    private final Router router;
    private final StaticFileService staticFileService;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public HttpServer(int port, Router router, StaticFileService staticFileService) {
        this.port = port;
        this.router = router;
        this.staticFileService = staticFileService;
    }

    public void start() {
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("==================================================");
            System.out.println("🚀 Custom Sequential HttpServer running on port: " + port);
            System.out.println("==================================================");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                } catch (IOException e) {
                    if (!running) {
                        System.out.println("Server socket closed cleanly.");
                        break;
                    }
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
            System.out.println("Server stopped gracefully.");
        } catch (IOException e) {
            System.err.println("Could not start server on port " + port + ": " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing ServerSocket: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket socket) {
        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isBlank()) {
                return;
            }

            String[] tokens = requestLine.split(" ");
            if (tokens.length < 2) {
                sendResponse(out, 400, "text/plain", "400 Bad Request".getBytes(StandardCharsets.UTF_8), null);
                return;
            }

            String method = tokens[0];
            URI uri = URI.create(tokens[1]);
            String path = uri.getPath();

            // Read headers
            Map<String, String> headers = new HashMap<>();
            String line;
            int contentLength = 0;
            while ((line = reader.readLine()) != null && !line.isBlank()) {
                int colonIdx = line.indexOf(':');
                if (colonIdx > 0) {
                    String headerName = line.substring(0, colonIdx).trim();
                    String headerVal = line.substring(colonIdx + 1).trim();
                    headers.put(headerName, headerVal);
                    if (headerName.equalsIgnoreCase("Content-Length")) {
                        try {
                            contentLength = Integer.parseInt(headerVal);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            // Read Body if any
            String body = "";
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                int read = reader.read(bodyChars, 0, contentLength);
                if (read > 0) {
                    body = new String(bodyChars, 0, read);
                }
            }

            // Parse Query Params
            Map<String, String> queryParams = parseQueryParams(uri.getQuery());
            Request req = new Request(method, path, queryParams, body);
            Response resp = new Response();

            // 1. Check registered routes in Router
            GetLambda routeHandler = router.getRoute(method, path);
            if (routeHandler != null) {
                try {
                    String result = routeHandler.handle(req, resp);
                    byte[] responseBytes = result != null ? result.getBytes(StandardCharsets.UTF_8) : new byte[0];
                    sendResponse(out, resp.getStatusCode(), resp.getContentType(), responseBytes, resp.getHeaders());
                } catch (Exception e) {
                    String errJson = "{\"error\":\"" + e.getMessage() + "\"}";
                    sendResponse(out, 500, "application/json", errJson.getBytes(StandardCharsets.UTF_8), null);
                }
                return;
            }

            // 2. Check Static File Service
            if (staticFileService != null) {
                byte[] fileBytes = staticFileService.getFileContent(path);
                if (fileBytes != null) {
                    String mimeType = staticFileService.getContentType(path);
                    sendResponse(out, 200, mimeType, fileBytes, null);
                    return;
                }
            }

            // 3. 404 Not Found
            sendResponse(out, 404, "text/plain; charset=utf-8", "404 Not Found".getBytes(StandardCharsets.UTF_8), null);

        } catch (Exception e) {
            System.err.println("Error processing request: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private void sendResponse(OutputStream out, int status, String contentType, byte[] body, Map<String, String> customHeaders) throws IOException {
        String statusText = getStatusText(status);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), false);
        
        writer.print("HTTP/1.1 " + status + " " + statusText + "\r\n");
        writer.print("Content-Type: " + contentType + "\r\n");
        writer.print("Content-Length: " + body.length + "\r\n");
        writer.print("Connection: close\r\n");

        if (customHeaders != null) {
            for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase("Content-Type") && !entry.getKey().equalsIgnoreCase("Content-Length")) {
                    writer.print(entry.getKey() + ": " + entry.getValue() + "\r\n");
                }
            }
        }
        writer.print("\r\n");
        writer.flush();

        out.write(body);
        out.flush();
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;

        for (String param : query.split("&")) {
            String[] entry = param.split("=", 2);
            if (entry.length > 1) {
                map.put(entry[0], entry[1]);
            } else {
                map.put(entry[0], "");
            }
        }
        return map;
    }

    private String getStatusText(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "OK";
        };
    }
}
