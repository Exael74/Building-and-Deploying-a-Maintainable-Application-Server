package edu.eci.tdse.webframework;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private int statusCode = 200;
    private String contentType = "application/json; charset=utf-8";
    private final Map<String, String> headers = new HashMap<>();

    public Response() {
        headers.put("Access-Control-Allow-Origin", "*");
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        this.headers.put("Content-Type", contentType);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeader(String name, String value) {
        this.headers.put(name, value);
    }
}
