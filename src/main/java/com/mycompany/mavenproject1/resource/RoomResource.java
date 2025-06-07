package com.mycompany.mavenproject1.resource;

import com.mycompany.mavenproject1.data.DataStore;
import com.mycompany.mavenproject1.exception.ResourceNotFoundException;
import com.mycompany.mavenproject1.exception.RoomNotEmptyException;
import com.mycompany.mavenproject1.model.Room;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

/**
 * JAX-RS Resource class for managing Room entities.
 * Handles CRUD operations on the /api/v1/rooms collection.
 * Includes safety logic to prevent deletion of rooms with active sensors.
 */
@Path("rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Returns a comprehensive list of all rooms on the campus.
     */
    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(dataStore.getRooms().values())).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. Auto-generates an ID if not provided.
     * Returns 201 Created with a Location header pointing to the new resource.
     */
    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room.getId() == null || room.getId().isEmpty()) {
            room.setId("ROOM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        dataStore.getRooms().put(room.getId(), room);

        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Fetches detailed metadata for a specific room.
     * Returns 404 if the room does not exist.
     */
    @GET
    @Path("{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room with ID '" + roomId + "' was not found.");
        }
        return Response.ok(room).build();
    }

    /**
     * PUT /api/v1/rooms/{roomId}
     * Updates an existing room's details while preserving sensor links.
     * Returns 404 if the room does not exist.
     */
    @PUT
    @Path("{roomId}")
    public Response updateRoom(@PathParam("roomId") String roomId, Room updatedRoom) {
        Room existing = dataStore.getRooms().get(roomId);
        if (existing == null) {
            throw new ResourceNotFoundException("Room with ID '" + roomId + "' was not found.");
        }

        // Preserve the ID and sensor links
        updatedRoom.setId(roomId);
        updatedRoom.setSensorIds(existing.getSensorIds());
        dataStore.getRooms().put(roomId, updatedRoom);

        return Response.ok(updatedRoom).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Decommissions a room. BUSINESS RULE: A room cannot be deleted if it still
     * has sensors assigned to it (returns 409 Conflict).
     * This operation is idempotent — deleting an already-deleted room returns 404.
     */
    @DELETE
    @Path("{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room with ID '" + roomId + "' was not found.");
        }

        // Safety check: prevent deletion if sensors are still assigned
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Cannot delete room '" + roomId + "'. It still has "
                + room.getSensorIds().size() + " sensor(s) assigned: "
                + room.getSensorIds()
                + ". Please reassign or remove all sensors before deleting this room."
            );
        }

        dataStore.getRooms().remove(roomId);
        return Response.noContent().build();
    }
}
