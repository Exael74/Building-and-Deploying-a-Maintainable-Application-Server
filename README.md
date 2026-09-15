# Maintainable Application Server

A small Java web framework that lets developers register HTTP GET services through lambda functions, serve static resources, and run as a maintainable, sequential application server — evolved from a hard-coded HTTP server into a reusable framework.

> **Project status:** core framework and sequential HTTP engine are implemented and tested locally (routing, static files, error handling). Environment-variable configuration, graceful shutdown, and cloud deployment are still in progress — sections below are marked accordingly.

---

## 1. Description

This project implements a lightweight application server on top of raw Java sockets (`java.net.ServerSocket`). Instead of hard-coding routes inside the connection loop, application developers register routes as lambda functions:

```java
WebFramework.staticfiles("webroot");

WebFramework.get("/hello", (req, resp) -> {
    String name = req.getValue("name");
    return "{\"message\": \"Hello, " + (name == null || name.isBlank() ? "World" : name) + "!\"}";
});

WebFramework.get("/pi", (req, resp) -> "{\"pi\": " + Math.PI + "}");

WebFramework.start(port);
```

The HTTP connection loop never needs to change when a new route is added — it only consults the `Router`.

---

## 2. Architecture

```
Application            registers routes and static file location
    │
    ▼
WebFramework            static facade: staticfiles(), get(), start(), stop()
    │
    ▼
Router                  maps (method, path) -> GetLambda handler
    │
    ▼
HttpServer               accepts connections sequentially, parses requests,
    │                    sends responses, dispatches to Router or StaticFileService
    ▼
Request / Response        represent parsed HTTP data (query params, status, headers)
    │
    ▼
StaticFileService         serves files from the classpath when no route matches
```

### Responsibilities of the main components

| Component | Responsibility |
|---|---|
| `Application` | Registers routes and static resource location. The only class an application developer needs to touch. |
| `WebFramework` | Static facade exposing `staticfiles()`, `get()`, `start()`, `stop()`. Hides socket/thread details from the developer. |
| `Router` | Stores registered routes and resolves an incoming `(method, path)` pair to a `GetLambda` handler. |
| `HttpServer` | Owns the `ServerSocket`, accepts one connection at a time, parses the raw HTTP request, and writes the raw HTTP response. Contains no application logic. |
| `Request` | Read-only view of the incoming request: method, path, and query parameters via `getValue(key)`. |
| `Response` | Lets a handler set the status code and headers (e.g. `Content-Type`) before the body is written. |
| `StaticFileService` | Resolves a request path to a classpath resource under the configured static root and infers its content type. |
| `GetLambda` | Functional interface `(Request, Response) -> String` — the contract every route handler implements. |

### Request flow

```
Incoming connection
      ↓
Parse HTTP method, path, and query string
      ↓
Router.resolve(method, path)
      ↓
 found? → run GetLambda handler → write response
      ↓ not found
StaticFileService.resolve(path)
      ↓
 found? → write file bytes with inferred Content-Type
      ↓ not found
Write 404 Not Found
```

### Architecture metaphor — an office building

| Building metaphor | Framework component |
|---|---|
| Entrance and receptionist | `HttpServer` — accepts every visitor (connection), reads what they're asking for |
| Directory in the lobby | `Router` — looks up which office handles the visitor's request |
| Individual offices | `GetLambda` handlers registered via `get()` — each implements one specific service |
| Document archive | `StaticFileService` — hands out pre-existing documents (HTML, CSS, JS, images) when no office needs to get involved |
| Building configuration | Environment variables (`PORT`, and more to come) — port and deployment-specific settings, not printed on the walls (hard-coded) |
| Closing procedure | Graceful shutdown *(planned)* — the building finishes serving the visitor currently at the desk before locking the doors |

This works because each part has **one job**: the receptionist doesn't know how to do office work, the directory doesn't answer questions itself, and the archive doesn't care what happens in the offices. Adding a new office (route) never requires retraining the receptionist (touching `HttpServer`).

---

## 3. Why this is maintainable

| Principle | Application in this project |
|---|---|
| Separation of concerns | HTTP parsing/socket handling (`HttpServer`) is separate from application behavior (`Application`'s lambdas). |
| Modularity | Routing, request/response modeling, and static files are each their own class. |
| Low coupling | Adding a route means calling `get(...)` in `Application`; `HttpServer` is never touched. |
| High cohesion | Each class has a single, focused responsibility (see table above). |
| Abstraction | Developers call `get()` / `staticfiles()` without ever touching a `Socket`. |
| Extensibility | New services are added by registering more lambdas — no new server code. |
| Testability | `Router`, `Request` parsing, and individual handlers can be tested in isolation from socket I/O. |
| Operational maintainability | The same source builds and runs identically locally and (once deployed) in the cloud. |

---

## 4. Build and run locally

Requirements: JDK 17+ and Maven.

```bash
mvn compile
mvn exec:java
```

The server starts on port `8080` by default (see [Environment variables](#5-environment-variables)) and binds to all interfaces.

Stop it with `Ctrl+C` for now — a graceful `/shutdown` route is planned (see status below).

---

## 5. Environment variables

| Variable | Purpose | Local default | Status |
|---|---|---|---|
| `PORT` | HTTP server port | `8080` | ✅ Implemented — read in `Application.main` via `System.getenv("PORT")` |
| `GREETING_PREFIX` | Prefix used by the `/hello` route | `Hello` | ⬜ Planned, not implemented yet |
| `APP_ENV` | Execution environment (`development` / `production`); will gate the `/shutdown` route | `development` | ⬜ Planned, not implemented yet |
| `STATIC_FILES_PATH` | Optional override for the static resources root | `webroot` | ⬜ Planned, not implemented yet |

No credentials or secrets are used or stored in this project.

---

## 6. Cloud deployment

**Not deployed yet.** This section will be completed with the chosen platform, the public URL, and reproduction steps once the remaining framework work (configuration, static-file hardening, graceful shutdown) is finished.

---

## 7. Example URLs (local)

| URL | Expected result |
|---|---|
| `GET /` | Static `index.html` |
| `GET /index.html` | Static HTML page |
| `GET /app.js` | Static JavaScript |
| `GET /styles.css` | Static CSS |
| `GET /images/logo.png` | Static binary image (`image/png`) |
| `GET /hello?name=Pedro` | Dynamic JSON response from a lambda: `{"message": "Hello, Pedro!"}` |
| `GET /pi` | Dynamic JSON response from a lambda: `{"pi": 3.141592653589793}` |
| `GET /api/status` | Dynamic JSON status response |
| `GET /unknown` | `404 Not Found` |

---

## 8. Tests performed

Manual verification against the running local server (`mvn exec:java` on port 8080):

- `GET /hello?name=Pedro` → `200 OK`, `{"message": "Hello, Pedro!"}`
- `GET /pi` → `200 OK`, `{"pi": 3.141592653589793}`
- `GET /` and `GET /app.js` → `200 OK` with correct `Content-Type` (`text/html`, `application/javascript`)
- `GET /images/logo.png` → `200 OK`, `image/png`, binary content served correctly
- `GET /unknown` → `404 Not Found`, `text/plain`
- Malformed request line sent over a raw socket → `400 Bad Request`, server keeps running and accepts the next connection (sequential loop, no crash)

## 9. Evidence (cloud deployment, environment variables, shutdown)

**Pending.** Screenshots and evidence for the deployed page, live REST responses, configured environment variables, and `/shutdown` behavior (available in development, absent in production) will be added once those parts are implemented and deployed.

---

## 10. Roadmap

- [x] Project structure and package separation (framework vs. application)
- [x] Sequential HTTP core (`ServerSocket`, no thread pool)
- [x] Lambda-based GET routing and query-string parsing
- [x] Static file serving, including binary resources
- [x] Basic error handling (404, 400, unhandled route exceptions)
- [ ] Full environment-variable configuration (`GREETING_PREFIX`, `APP_ENV`, `STATIC_FILES_PATH`)
- [ ] Graceful, sequential `/shutdown` route (development only)
- [ ] Cloud deployment and public URL
