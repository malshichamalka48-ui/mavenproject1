package com.mycompany.mavenproject1.data;

import com.mycompany.mavenproject1.model.Room;
import com.mycompany.mavenproject1.model.Sensor;
import com.mycompany.mavenproject1.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store for the Smart Campus API.
 * Uses ConcurrentHashMap for thread-safe access across concurrent requests.
 * Pre-populated with sample data for demonstration purposes.
 */
public class DataStore {

    private static DataStore instance;

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
        initSampleData();
    }

    /**
     * Returns the singleton instance of the DataStore.
     * Synchronized to prevent multiple instantiations in a multi-threaded environment.
     */
    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    /**
     * Pre-populates the data store with sample rooms, sensors, and readings
     * so the API has data available for immediate testing and demonstration.
     */
    private void initSampleData() {
        // --- Sample Rooms ---
        Room room1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room room2 = new Room("ENG-101", "Engineering Lab A", 30);
        Room room3 = new Room("SCI-202", "Science Lecture Hall", 120);

        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);
        rooms.put(room3.getId(), room3);

        // --- Sample Sensors ---
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 415.0, "ENG-101");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 28.0, "SCI-202");

        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        // Link sensors to their respective rooms
        room1.getSensorIds().add(s1.getId());
        room2.getSensorIds().add(s2.getId());
        room3.getSensorIds().add(s3.getId());

        // Initialize empty reading lists for each sensor
        sensorReadings.put(s1.getId(), new ArrayList<>());
        sensorReadings.put(s2.getId(), new ArrayList<>());
        sensorReadings.put(s3.getId(), new ArrayList<>());

        // Add a couple of sample readings for TEMP-001
        SensorReading r1 = new SensorReading(System.currentTimeMillis() - 60000, 21.8);
        SensorReading r2 = new SensorReading(System.currentTimeMillis(), 22.5);
        sensorReadings.get("TEMP-001").add(r1);
        sensorReadings.get("TEMP-001").add(r2);
    }

    // --- Accessor Methods ---

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Map<String, List<SensorReading>> getSensorReadings() {
        return sensorReadings;
    }
}
