package main.java.com.carrental.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handles static .html files for the webserver
 */
public class StaticFileHandler implements HttpHandler {

	private String basePath;
	
	public StaticFileHandler(String basePath) {
		this.basePath = basePath;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
	      String path = exchange.getRequestURI().getPath();
	        if (path.equals("/")) {
	            path = "/login.html";
	        }
	        Path filePath = Paths.get(basePath, path).normalize();
	        if (!filePath.startsWith(basePath)) {
	            send404(exchange);
	            return;
	        }
	        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
	            String mime = Files.probeContentType(filePath);
	            if (mime == null) mime = "application/octet-stream";
	            exchange.getResponseHeaders().set("Content-Type", mime);
	            exchange.sendResponseHeaders(200, Files.size(filePath));
	            Files.copy(filePath, exchange.getResponseBody());
	        } else {
	            send404(exchange);
	        }
	        exchange.getResponseBody().close();
		
	}
	
	/**
	 * Sends a 404 not found error message to the web server client
	 * @param exchange
	 * @throws IOException
	 */
    private void send404(HttpExchange exchange) throws IOException {
        String response = "404 Not Found";
        exchange.sendResponseHeaders(404, response.length());
        exchange.getResponseBody().write(response.getBytes());
    }

}