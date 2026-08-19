import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiHealthChecker {

    private static final Logger logger = LoggerFactory.getLogger(ApiHealthChecker.class);
    private static final int DEFAULT_PORT = 8000;
    private static final int CHECK_INTERVAL_MINUTES = 5;
    private static final List<String> API_URLS = List.of(
            "https://jsonplaceholder.typicode.com/posts/1",
            "https://jsonplaceholder.typicode.com/users",
            "https://jsonplaceholder.typicode.com/comments");

    private final HttpClient client;

    public ApiHealthChecker() {
        this.client = HttpClient.newHttpClient();
    }

    public ApiHealthChecker(HttpClient client) {
        this.client = client;
    }

    public static void main(String[] args) {
        ApiHealthChecker checker = new ApiHealthChecker();
        checker.start();
    }

    public void start() {
        int port = resolvePort();
        runChecks();
        startHttpServer(port);
        scheduleChecks();
        keepRunning();
    }

    public void checkApi(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            logger.info("API: {}", url);
            logger.info("Status Code: {}", response.statusCode());

            if (response.statusCode() == 200) {
                logger.info("✓ API is UP");
            } else {
                logger.warn("✗ API returned an unexpected status");
            }

            logger.info("--------------------------------");

        } catch (IOException e) {
            logger.error("API: {} is DOWN (IO error)", url, e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("API: {} was interrupted", url, e);
        }
    }

    private void runChecks() {
        for (String url : API_URLS) {
            checkApi(url);
        }
    }

    private void startHttpServer(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", this::handleRoot);
            server.createContext("/health", this::handleHealth);
            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();
            logger.info("HTTP server started on port {}", port);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start HTTP server on port " + port, e);
        }
    }

    private void scheduleChecks() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::runChecks, CHECK_INTERVAL_MINUTES, CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    private void keepRunning() {
        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Application interrupted, shutting down");
        }
    }

    private int resolvePort() {
        String rawPort = System.getenv("PORT");
        if (rawPort == null || rawPort.isBlank()) {
            return DEFAULT_PORT;
        }

        try {
            return Integer.parseInt(rawPort);
        } catch (NumberFormatException e) {
            logger.warn("Invalid PORT value '{}', falling back to {}", rawPort, DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }

    private void handleRoot(HttpExchange exchange) throws IOException {
        writeResponse(exchange, 200, "ApiHealthChecker is running\n");
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        writeResponse(exchange, 200, "OK\n");
    }

    private void writeResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(responseBytes);
        } finally {
            exchange.close();
        }
    }
}
