package com.ineat.quarkus.poc;

import io.smallrye.reactive.messaging.annotations.Stream;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/thermal")
public class ThermalResource {

    @Inject
    @Stream("thermal-stream")
    Publisher<Integer> temps;


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String thermal() {
        System.out.println("TEST");
        return "Temp : " + temps;
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<Integer> stream() {
        return temps;
    }
}