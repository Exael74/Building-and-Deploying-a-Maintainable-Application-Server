package edu.eci.tdse.webframework;

import java.util.Collections;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;
    private final String body;

    public Request(String method, String path, Map<String, String> queryParams, String body) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams != null ? queryParams : Collections.emptyMap();
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getBody() {
        return body;
    }

    /**
     * Retorna el valor del parámetro de consulta (query param).
     * Retorna null si no existe, sin lanzar excepción.
     */
    public String getValue(String key) {
        return queryParams.get(key);
    }
}
