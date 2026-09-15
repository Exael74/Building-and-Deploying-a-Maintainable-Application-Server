package edu.eci.tdse.app;

import edu.eci.tdse.webframework.WebFramework;

public class Application {
    public static void main(String[] args) {
        // 1. Leer puerto de variable de entorno o usar 8080 por defecto
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null && !portEnv.isBlank()) ? Integer.parseInt(portEnv) : 8080;

        // 2. Configurar ubicación de archivos estáticos
        WebFramework.staticfiles("webroot");

        // 3. Registrar endpoints usando las firmas exactas pedidas: (req, resp) -> String
        WebFramework.get("/hello", (req, resp) -> {
            String name = req.getValue("name");
            if (name == null || name.isBlank()) {
                name = "World";
            }
            return "{\"message\": \"Hello, " + name + "!\"}";
        });

        WebFramework.get("/pi", (req, resp) -> {
            return "{\"pi\": " + Math.PI + "}";
        });

        WebFramework.get("/api/status", (req, resp) -> {
            return "{\"status\": \"UP\", \"framework\": \"Sequential Custom HttpServer\"}";
        });

        // 4. Iniciar servidor web
        WebFramework.start(port);
    }
}
