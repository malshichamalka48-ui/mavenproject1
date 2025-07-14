package com.mycompany.mavenproject1.exception;

import com.mycompany.mavenproject1.model.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps LinkedResourceNotFoundException to HTTP 422 Unprocessable Entity.
 * Triggered when a sensor is registered with a roomId that does not exist.
 *
 * HTTP 422 is more semantically accurate than 404 here because:
 * - The request itself was well-formed (valid JSON)
 * - The issue is a missing reference INSIDE the payload, not a missing URL resource
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        ErrorResponse error = new ErrorResponse(
                422,
                "Unprocessable Entity",
                exception.getMessage()
        );
        return Response.status(422)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
