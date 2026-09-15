package edu.eci.tdse.webframework;

@FunctionalInterface
public interface GetLambda {
    String handle(Request req, Response resp);
}
