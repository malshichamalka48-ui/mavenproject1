package com.mycompany.mavenproject1.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * JAX-RS filter that implements both ContainerRequestFilter and ContainerResponseFilter
 * to provide API observability through request/response logging.
 *
 * Using filters for cross-cutting concerns like logging is advantageous because:
 * - It avoids repetitive Logger.info() statements in every resource method
 * - It ensures consistent logging across ALL endpoints automatically
 * - New endpoints are logged without any additional code
 * - The logging logic is centralized and easy to maintain
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Logs the HTTP method and request URI for every incoming request.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        LOGGER.info(">> Incoming Request: " + method + " " + uri);
    }

    /**
     * Logs the HTTP status code for every outgoing response.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        int status = responseContext.getStatus();
        LOGGER.info("<< Outgoing Response: " + method + " " + uri + " => HTTP " + status);
    }
}
