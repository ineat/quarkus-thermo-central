package com.ineat.quarkus.poc;

import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/sensors")
public class SensorResource {

    @ConfigProperty(name = "topics", defaultValue = "Config not set")
    Provider<String> consulAuthorizedTopics;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject topics() {
        JsonObject config = new JsonObject(consulAuthorizedTopics.get());
        return config;
    }

}