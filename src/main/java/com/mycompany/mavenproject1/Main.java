package com.mycompany.mavenproject1;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the Smart Campus API.
 * Bootstraps an embedded Grizzly HTTP server with Jersey JAX-RS support.
 */
public class Main {

    /** Base URI the Grizzly HTTP server will listen on. */
    public static final String BASE_URI = "http://localhost:8080/";
    public static final String API_URI = BASE_URI + "api/v1/";

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Creates and starts the Grizzly HTTP server with the JAX-RS application configuration.
     * @return the running HttpServer instance
     */
    public static HttpServer startServer() {
        final JakartaRestConfiguration config = new JakartaRestConfiguration();
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    /**
     * Main method — starts the server and waits for user input to stop.
     */
    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();

            LOGGER.log(Level.INFO, "===================================================");
            LOGGER.log(Level.INFO, " Smart Campus API started successfully!");
            LOGGER.log(Level.INFO, " Base URI: {0}", API_URI);
            LOGGER.log(Level.INFO, " Discovery: {0}", API_URI);
            LOGGER.log(Level.INFO, " Rooms:     {0}rooms", API_URI);
            LOGGER.log(Level.INFO, " Sensors:   {0}sensors", API_URI);
            LOGGER.log(Level.INFO, "===================================================");

            System.out.println("\nSmart Campus API is running at " + API_URI);
            System.out.println("Press ENTER to stop the server...");
            System.in.read();

            server.shutdownNow();
            LOGGER.log(Level.INFO, "Server stopped.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start server", e);
        }
    }
}
