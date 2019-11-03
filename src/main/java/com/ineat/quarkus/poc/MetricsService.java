package com.ineat.quarkus.poc;

import io.vertx.core.json.JsonObject;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MetricsService {

    public void create(JsonObject metric) {
        System.out.println(metric);
        // Persistence coming soon...
    }
}
