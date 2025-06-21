package com.mycompany.mavenproject1.resource;

import com.mycompany.mavenproject1.data.DataStore;
import com.mycompany.mavenproject1.exception.SensorUnavailableException;
import com.mycompany.mavenproject1.model.Sensor;
import com.mycompany.mavenproject1.model.SensorReading;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-Resource class for managing SensorReading entities.
 * This class is NOT annotated with @Path — it is instantiated by the
 * sub-resource locator method in SensorResource.
 *
 * Handles the historical reading data for a specific sensor context.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    /**
     * Constructor receives the parent sensor ID from the sub-resource locator.
     * @param sensorId the ID of the sensor whose readings are being managed
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Fetches the complete history of readings for this sensor.
     */
    @GET
    public Response getAllReadings() {
        List<SensorReading> readings = dataStore.getSensorReadings()
                .getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(readings).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading to this sensor's history.
     *
     * BUSINESS RULES:
     * 1. If the sensor status is "MAINTENANCE", the reading is rejected (403 Forbidden).
     * 2. On success, the parent sensor's currentValue is updated to reflect the new reading.
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensors().get(sensorId);

        // State Constraint: sensors in MAINTENANCE cannot accept new readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is currently in MAINTENANCE mode "
                + "and cannot accept new readings. Please set the sensor to "
                + "ACTIVE status before submitting readings."
            );
        }

        // Auto-set timestamp if not provided
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Store the reading
        dataStore.getSensorReadings()
                .computeIfAbsent(sensorId, k -> new ArrayList<>())
                .add(reading);

        // Side Effect: update the parent sensor's currentValue for data consistency
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings/{readingId}
     * Fetches a specific reading by its ID.
     */
    @GET
    @Path("{readingId}")
    public Response getReading(@PathParam("readingId") String readingId) {
        List<SensorReading> readings = dataStore.getSensorReadings()
                .getOrDefault(sensorId, new ArrayList<>());

        return readings.stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst()
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Reading not found\"}")
                        .build());
    }
}
