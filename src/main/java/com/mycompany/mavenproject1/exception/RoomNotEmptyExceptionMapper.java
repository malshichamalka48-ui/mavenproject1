package com.mycompany.mavenproject1.exception;

import com.mycompany.mavenproject1.model.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps RoomNotEmptyException to HTTP 409 Conflict.
 * Triggered when attempting to delete a room that still has sensors assigned.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        ErrorResponse error = new ErrorResponse(
                409,
                "Conflict",
                exception.getMessage()
        );
        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
