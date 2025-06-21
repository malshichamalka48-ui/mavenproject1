package com.mycompany.mavenproject1.resource;

import com.mycompany.mavenproject1.data.DataStore;
import com.mycompany.mavenproject1.exception.LinkedResourceNotFoundException;
import com.mycompany.mavenproject1.exception.ResourceNotFoundException;
import com.mycompany.mavenproject1.model.Room;
import com.mycompany.mavenproject1.model.Sensor;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JAX-RS Resource class for managing Sensor entities.
 * Handles CRUD operations on the /api/v1/sensors collection.
 * Supports filtered retrieval via @QueryParam and sub-resource delegation for readings.
 */
@Path("sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/sensors
     * Returns all sensors. Supports optional filtering by type via query parameter.
     * Example: GET /api/v1/sensors?type=CO2
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors;

        if (type != null && !type.isEmpty()) {
            // Filter sensors by type (case-insensitive)
            sensors = dataStore.getSensors().values().stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        } else {
            sensors = new ArrayList<>(dataStore.getSensors().values());
        }

        return Response.ok(sensors).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor. Validates that the specified roomId exists in the system.
     * If the roomId does not exist, throws LinkedResourceNotFoundException (422).
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        // Validate that roomId refers to an existing room
        if (sensor.getRoomId() == null || !dataStore.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "The specified room with ID '" + sensor.getRoomId()
                + "' does not exist in the system. Cannot register sensor. "
                + "Please provide a valid roomId."
            );
        }

        // Auto-generate ID if not provided
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            sensor.setId("SENSOR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Store sensor
        dataStore.getSensors().put(sensor.getId(), sensor);

        // Link sensor to its room
        Room room = dataStore.getRooms().get(sensor.getRoomId());
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        // Initialize empty readings list
        dataStore.getSensorReadings().putIfAbsent(sensor.getId(), new ArrayList<>());

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Retrieves a specific sensor by its ID.
     */
    @GET
    @Path("{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }
        return Response.ok(sensor).build();
    }

    /**
     * PUT /api/v1/sensors/{sensorId}
     * Updates an existing sensor's details.
     */
    @PUT
    @Path("{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedSensor) {
        Sensor existing = dataStore.getSensors().get(sensorId);
        if (existing == null) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }

        updatedSensor.setId(sensorId);
        dataStore.getSensors().put(sensorId, updatedSensor);
        return Response.ok(updatedSensor).build();
    }

    /**
     * DELETE /api/v1/sensors/{sensorId}
     * Removes a sensor and unlinks it from its parent room.
     */
    @DELETE
    @Path("{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }

        // Remove sensor from its room's sensorIds list
        Room room = dataStore.getRooms().get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        // Remove sensor and its readings
        dataStore.getSensors().remove(sensorId);
        dataStore.getSensorReadings().remove(sensorId);

        return Response.noContent().build();
    }

    /**
     * Sub-Resource Locator for sensor readings.
     * Delegates all requests to /api/v1/sensors/{sensorId}/readings
     * to the SensorReadingResource class.
     * This pattern keeps the code modular and manageable.
     */
    @Path("{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        // Validate sensor exists before delegating
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
