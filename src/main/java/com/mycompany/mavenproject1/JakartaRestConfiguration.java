package com.mycompany.mavenproject1;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Configures Jakarta RESTful Web Services for the Smart Campus application.
 * Extends ResourceConfig (which is a subclass of jakarta.ws.rs.core.Application)
 * and uses @ApplicationPath to define the versioned API entry point.
 *
 * JAX-RS Resource Lifecycle:
 * By default, JAX-RS creates a NEW instance of each resource class for every incoming
 * request (per-request lifecycle). This means instance fields are not shared between
 * requests. To safely share data, we use a singleton DataStore with ConcurrentHashMap.
 *
 * Note: The API base path "/api/v1" is configured in Main.java via the Grizzly server
 * URI. When deployed to a servlet container, @ApplicationPath("/api/v1") would be used
 * instead. For embedded Grizzly, we set ApplicationPath to "/" to avoid path duplication.
 */
@ApplicationPath("/api/v1")
public class JakartaRestConfiguration extends ResourceConfig {

    public JakartaRestConfiguration() {
        // Scan these packages for resource classes, exception mappers, and filters
        packages(
            "com.mycompany.mavenproject1.resource",
            "com.mycompany.mavenproject1.exception",
            "com.mycompany.mavenproject1.filter"
        );
    }
}
