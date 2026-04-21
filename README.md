# Smart Campus Sensor & Room Management API

## Overview

This project implements a RESTful API for the university's **Smart Campus** initiative using **JAX-RS (Jakarta RESTful Web Services)** with **Jersey** as the implementation and **Grizzly** as the embedded HTTP server. The API manages Rooms, Sensors, and SensorReadings across the campus infrastructure, providing a seamless interface for facilities managers and automated building systems.

### Architecture

- **Framework**: JAX-RS 3.1 (Jersey 3.1.5)
- **Server**: Embedded Grizzly HTTP Server
- **Data Storage**: In-memory using `ConcurrentHashMap` and `ArrayList`
- **JSON**: Jackson (via `jersey-media-json-jackson`)
- **Build**: Maven with Java 17

### Resource Hierarchy

```
/api/v1/                          → Discovery (API metadata & links)
/api/v1/rooms                     → Room collection
/api/v1/rooms/{roomId}            → Individual room
/api/v1/sensors                   → Sensor collection
/api/v1/sensors?type={type}       → Filtered sensors
/api/v1/sensors/{sensorId}        → Individual sensor
/api/v1/sensors/{sensorId}/readings     → Sensor readings (sub-resource)
```

---

## How to Build and Run

### Prerequisites

- **Java 17** or higher installed
- **Apache Maven 3.6+** installed
- `JAVA_HOME` environment variable set

### Step 1: Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/smartcampus-api.git
cd smartcampus-api
```

### Step 2: Build the Project

```bash
mvn clean compile
```

### Step 3: Launch the Server

```bash
mvn exec:java
```

The server will start at **http://localhost:8080/api/v1/**. You will see:
```
Smart Campus API is running at http://localhost:8080/api/v1/
Press ENTER to stop the server...
```

### Step 4: Test the API

Use the sample `curl` commands below or open Postman to interact with the API.

---

## Sample curl Commands

### 1. Discovery Endpoint — Get API Info
```bash
curl -X GET http://localhost:8080/api/v1/
```

### 2. Get All Rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 3. Create a New Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "CS-401", "name": "Computer Science Lab", "capacity": 40}'
```

### 4. Get a Specific Room
```bash
curl -X GET http://localhost:8080/api/v1/rooms/LIB-301
```

### 5. Create a New Sensor (linked to an existing room)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "TEMP-002", "type": "Temperature", "status": "ACTIVE", "currentValue": 21.0, "roomId": "LIB-301"}'
```

### 6. Get Sensors Filtered by Type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 7. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 23.5, "timestamp": 1713800000000}'
```

### 8. Get Sensor Readings History
```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

### 9. Try Deleting a Room with Sensors (triggers 409 Conflict)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 10. Try Posting a Reading to a MAINTENANCE Sensor (triggers 403 Forbidden)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 35.0}'
```

---

## Report — Answers to Coursework Questions

### Part 1: Service Architecture & Setup

**Q1: What is the default lifecycle of a JAX-RS Resource class?**

By default, JAX-RS uses a **per-request lifecycle** for resource classes. This means the JAX-RS runtime creates a **new instance** of the resource class for every incoming HTTP request. Once the request has been processed and the response sent, the instance is discarded and eligible for garbage collection.

This architectural decision has significant implications for in-memory data management:

- **Instance fields are NOT shared** between requests. If a resource class stored data in an instance variable (e.g., a `HashMap`), each request would get its own empty map, and any data added during the request would be lost immediately after.
- To persist data across requests, we must use a **shared, external data structure**. In this project, we use a **singleton `DataStore` class** that holds `ConcurrentHashMap` instances. Every resource class obtains the same `DataStore` instance via `DataStore.getInstance()`.
- Since multiple requests can arrive concurrently (each on its own thread with its own resource instance), the shared data structures must be **thread-safe**. We use `ConcurrentHashMap` which provides thread-safe read/write operations without explicit synchronization, preventing race conditions and data corruption.

**Q2: Why is HATEOAS considered a hallmark of advanced RESTful design?**

HATEOAS (Hypermedia as the Engine of Application State) is considered the highest level of REST maturity (Level 3 in the Richardson Maturity Model). It means that API responses include **hyperlinks to related resources and available actions**, allowing clients to navigate the API dynamically.

Benefits over static documentation:
1. **Self-Discovery**: Clients do not need to hard-code URLs. They can start from a single entry point (the Discovery endpoint) and follow links to discover all available resources.
2. **Decoupling**: If the server changes its URL structure, clients that follow links dynamically will continue to work without code changes, whereas clients with hard-coded URLs would break.
3. **Reduced Documentation Dependency**: The API itself becomes its own documentation. New resources and actions are automatically discoverable through the links provided in responses.
4. **Evolvability**: The server can add new resources or modify paths without breaking existing clients, as long as the link relations remain consistent.

---

### Part 2: Room Management

**Q3: When returning a list of rooms, what are the implications of returning only IDs versus full objects?**

| Approach | Advantages | Disadvantages |
|----------|-----------|---------------|
| **IDs only** | Smaller payload size, lower bandwidth usage, faster response times for large collections | Clients must make additional HTTP requests for each room's details (N+1 problem), increasing latency and server load |
| **Full objects** | Clients get all needed data in one request, reducing round-trips and simplifying client-side logic | Larger payload size, more bandwidth consumption, potentially slower for very large collections with many fields |

In this implementation, we return **full room objects** because:
- The room model is relatively lightweight (4 fields)
- It eliminates the N+1 query problem
- Campus management dashboards typically need to display all room details at once
- For very large collections, pagination could be added as a future enhancement

**Q4: Is the DELETE operation idempotent in your implementation?**

In our implementation, DELETE is **conditionally idempotent** in terms of the server-side effect (the resource state). Here is what happens when a client sends the same DELETE request multiple times:

1. **First request**: The room is found and deleted. The server returns **204 No Content**, indicating successful deletion.
2. **Second (and subsequent) requests**: The room no longer exists in the data store. The server throws a `ResourceNotFoundException`, which is mapped to **404 Not Found**.

While the side-effect is the same after the first call (the room remains deleted), the response code changes from 204 to 404. Strictly speaking, the HTTP specification defines idempotency as "the server state after multiple identical requests is the same as after a single request" — which is satisfied. The different response codes are acceptable because they accurately reflect the current state of the resource. Some implementations return 204 for already-deleted resources to be fully idempotent in response codes, but our approach is more informative and widely used in practice.

---

### Part 3: Sensor Operations & Linking

**Q5: What happens when a client sends data in a format other than application/json to a @Consumes(APPLICATION_JSON) endpoint?**

When we annotate a method with `@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS restricts the accepted `Content-Type` to `application/json` only. If a client sends a request with a different `Content-Type` header (e.g., `text/plain` or `application/xml`), the following occurs:

1. The JAX-RS runtime inspects the `Content-Type` header of the incoming request.
2. It finds that no resource method matches the content type.
3. JAX-RS automatically returns **HTTP 415 Unsupported Media Type** without ever invoking the resource method.

This is handled entirely by the JAX-RS framework — no custom code is needed. The 415 response informs the client that the server understood the request but refuses to process it because the payload format is not supported. The client must resend the request with `Content-Type: application/json`.

**Q6: Why is @QueryParam generally superior to path segments for filtering collections?**

Using `@QueryParam` (e.g., `GET /sensors?type=CO2`) is generally considered superior to embedding filter criteria in the URL path (e.g., `GET /sensors/type/CO2`) for several reasons:

1. **Semantic Clarity**: URL path segments should identify **resources**, not filter criteria. `/sensors/type/CO2` implies that "type" and "CO2" are sub-resources, which is misleading. Query parameters clearly indicate optional filtering criteria.
2. **Optionality**: Query parameters are inherently optional. The same endpoint (`GET /sensors`) works with or without the `?type=` parameter. With path segments, you would need separate route definitions for filtered and unfiltered requests.
3. **Multiple Filters**: Query parameters naturally support combining multiple filters (e.g., `?type=CO2&status=ACTIVE`). Path-based filtering becomes unwieldy with multiple criteria: `/sensors/type/CO2/status/ACTIVE`.
4. **Cacheability**: Query strings are part of the URI for caching purposes, and cache systems are designed to handle them. Path-based filtering creates many distinct URL paths that may confuse caching strategies.
5. **REST Convention**: The REST community and HTTP specification both treat query strings as the standard mechanism for filtering, sorting, and pagination of collections.

---

### Part 4: Deep Nesting with Sub-Resources

**Q7: What are the architectural benefits of the Sub-Resource Locator pattern?**

The Sub-Resource Locator pattern provides significant architectural benefits:

1. **Separation of Concerns**: Each resource class is responsible for a single entity type. `SensorResource` handles sensor CRUD, while `SensorReadingResource` handles readings. This follows the Single Responsibility Principle.
2. **Reduced Complexity**: Without sub-resource locators, all endpoints (sensors CRUD + readings CRUD) would be crammed into one massive controller class. As the API grows (e.g., adding sensor alerts, maintenance logs), the class would become unmanageable. Sub-resources keep each class focused and small.
3. **Reusability**: The `SensorReadingResource` class can potentially be reused in different contexts or tested independently.
4. **Contextual Delegation**: The sub-resource locator method validates the parent resource (sensor) exists before creating the sub-resource instance, centralising this validation. The sub-resource then operates within the guaranteed context of a valid parent.
5. **Team Scalability**: In larger teams, different developers can work on different sub-resource classes without merge conflicts in a single monolithic controller.
6. **Clean URL Structure**: The pattern naturally produces clean, hierarchical URLs (`/sensors/{id}/readings`) that reflect the domain model's parent-child relationships.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging

**Q8: Why is HTTP 422 more semantically accurate than 404 when the issue is a missing reference inside a valid JSON payload?**

HTTP 404 (Not Found) means "the requested resource at this URL does not exist." When a client sends `POST /api/v1/sensors` with a body containing a non-existent `roomId`, the URL itself (`/sensors`) is perfectly valid — the client is trying to create a new sensor, not access a missing URL.

HTTP 422 (Unprocessable Entity) means "the server understood the request and the syntax is correct, but it cannot process the contained instructions due to semantic errors." This is precisely our situation:
- The JSON payload is syntactically valid ✓
- The request URL is correct ✓
- But the **content** of the payload references a resource (`roomId`) that doesn't exist ✗

Using 404 would be misleading because it implies the `/sensors` endpoint itself doesn't exist, or that the client navigated to a wrong URL. HTTP 422 accurately conveys that the problem lies within the data the client submitted, not with the URL they accessed.

**Q9: What are the cybersecurity risks of exposing internal Java stack traces?**

Exposing raw Java stack traces to external API consumers creates several security vulnerabilities:

1. **Technology Fingerprinting**: Stack traces reveal the programming language (Java), framework (Jersey/JAX-RS), and library versions, allowing attackers to search for known vulnerabilities (CVEs) in those specific versions.
2. **Internal Architecture Exposure**: Package names (e.g., `com.mycompany.mavenproject1.resource.RoomResource`) reveal the internal code structure, class names, and module organization, helping attackers understand the system's architecture.
3. **Code Path Information**: Stack traces show the exact sequence of method calls, revealing business logic flow and potential weak points where validation or security checks might be bypassed.
4. **Database/File Information**: Stack traces from database or file system errors can expose table names, column names, file paths, and connection strings.
5. **Dependency Analysis**: Exception class names from third-party libraries reveal the full dependency chain, each representing a potential attack surface.

Our `GenericExceptionMapper` mitigates these risks by logging the full exception internally (for developer debugging) while returning only a generic, safe error message to the client.

**Q10: Why is it advantageous to use JAX-RS filters for logging rather than manual Logger.info() calls?**

Using JAX-RS filters (`ContainerRequestFilter` and `ContainerResponseFilter`) for cross-cutting concerns like logging is advantageous because:

1. **DRY Principle**: Without filters, every single resource method would need `Logger.info("Received GET /rooms")` at the start and `Logger.info("Returning 200")` at the end. This is repetitive and error-prone.
2. **Automatic Coverage**: Filters intercept ALL requests and responses automatically. When new endpoints are added, they are logged without any additional code.
3. **Consistency**: A centralised filter guarantees uniform log formatting across every endpoint. Manual logging often leads to inconsistent message formats.
4. **Separation of Concerns**: Resource methods should focus on business logic, not infrastructure concerns like logging. Filters cleanly separate these concerns.
5. **Maintainability**: To change the log format (e.g., adding request duration or client IP), you modify one filter class instead of hundreds of resource methods.
6. **AOP Pattern**: This follows the Aspect-Oriented Programming (AOP) paradigm where cross-cutting concerns are modularised into reusable components, which is an industry best practice.

---

## Project Structure

```
src/main/java/com/mycompany/mavenproject1/
├── Main.java                                    # Grizzly server bootstrap
├── JakartaRestConfiguration.java                # @ApplicationPath("/api/v1")
├── model/
│   ├── Room.java                                # Room POJO
│   ├── Sensor.java                              # Sensor POJO
│   ├── SensorReading.java                       # SensorReading POJO
│   └── ErrorResponse.java                       # Standard error response POJO
├── data/
│   └── DataStore.java                           # Singleton in-memory data store
├── resource/
│   ├── DiscoveryResource.java                   # GET /api/v1 (HATEOAS discovery)
│   ├── RoomResource.java                        # /api/v1/rooms CRUD
│   ├── SensorResource.java                      # /api/v1/sensors CRUD + filtering
│   └── SensorReadingResource.java               # Sub-resource for readings
├── exception/
│   ├── ResourceNotFoundException.java           # → 404
│   ├── RoomNotEmptyException.java               # → 409
│   ├── LinkedResourceNotFoundException.java     # → 422
│   ├── SensorUnavailableException.java          # → 403
│   ├── ResourceNotFoundExceptionMapper.java     # Maps to 404
│   ├── RoomNotEmptyExceptionMapper.java         # Maps to 409
│   ├── LinkedResourceNotFoundExceptionMapper.java # Maps to 422
│   ├── SensorUnavailableExceptionMapper.java    # Maps to 403
│   └── GenericExceptionMapper.java              # Catch-all → 500
└── filter/
    └── LoggingFilter.java                       # Request/Response logging
```
