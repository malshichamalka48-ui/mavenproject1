package com.mycompany.mavenproject1.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root "Discovery" endpoint providing API metadata and navigation links.
 * Implements HATEOAS principles by providing resource collection URLs.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiInfo(@Context UriInfo uriInfo) {
        String baseUri = uriInfo.getBaseUri().toString();

        Map<String, Object> apiInfo = new LinkedHashMap<>();
        apiInfo.put("name", "Smart Campus Sensor & Room Management API");
        apiInfo.put("version", "1.0");
        apiInfo.put("description", "RESTful API for managing rooms, sensors, and sensor readings across the university Smart Campus infrastructure.");

        // Administrative contact details
        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("name", "Smart Campus Admin");
        contact.put("email", "admin@smartcampus.westminster.ac.uk");
        contact.put("department", "Facilities Management");
        apiInfo.put("contact", contact);

        // HATEOAS-style resource links for client navigation
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", baseUri + "rooms");
        resources.put("sensors", baseUri + "sensors");
        apiInfo.put("resources", resources);

        return Response.ok(apiInfo).build();
    }
}
