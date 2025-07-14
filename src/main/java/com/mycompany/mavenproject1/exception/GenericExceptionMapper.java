package com.mycompany.mavenproject1.exception;

import com.mycompany.mavenproject1.model.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "catch-all" exception mapper that intercepts ANY unexpected runtime errors.
 * Returns a generic HTTP 500 Internal Server Error without exposing internal stack traces.
 *
 * Security: Exposing Java stack traces to external consumers is a security risk because
 * attackers can learn about class names, package structures, library versions, and
 * internal logic — information useful for crafting targeted exploits.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full exception internally for debugging
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by global mapper", exception);

        // Return a safe, generic error response — never expose internal details
        ErrorResponse error = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred on the server. Please contact the system administrator."
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
