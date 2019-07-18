package com.ineat.quarkus.poc;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;

@ApplicationScoped
public class ThermalProcessor {

    @Inject
    MetricsService metricsService;

    @ConfigProperty(name = "topics", defaultValue = "Config not set")
    Provider<String> consulAuthorizedTopics;

    @Incoming("topic-thermal")
    @Outgoing("converted-message")
    @Broadcast
    public JsonObject convertMessageToJsonObject(MqttMessage<byte[]> message) {
        JsonObject msg = new JsonObject()
                .put("topic", message.getTopic())
                .put("value", Double.valueOf(new String(message.getPayload())));
        message.ack();
        return  msg;
    }


    @Incoming("converted-message")
    @Outgoing("filtered-message-by-area")
    @Broadcast
    public PublisherBuilder<JsonObject> filterByArea(JsonObject message) {
        String[] splittedMessage = message.getString("topic").split("/");
        String areaName = splittedMessage[0];
        return ReactiveStreams.of(message).filter(s ->
            Optional.ofNullable(consulAuthorizedTopics.get())
                    .map(str -> new JsonObject(str))
                    .filter(config -> config.containsKey(areaName))
                    .isPresent()
        );
    }

    @Incoming("filtered-message-by-area")
    @Outgoing("filtered-message-by-kind")
    @Broadcast
    public PublisherBuilder<JsonObject> filterBykind(JsonObject message) {
        String[] splittedMessage = message.getString("topic").split("/");
        String areaName = splittedMessage[0];
        String kindName = splittedMessage[1];
        return ReactiveStreams.of(message).filter(s ->
                Optional.ofNullable(consulAuthorizedTopics.get())
                        .map(str -> new JsonObject(str))
                        .map(config -> config.getJsonObject(areaName))
                        .filter(area -> area.containsKey(kindName))
                        .isPresent()
        );
    }

    @Incoming("filtered-message-by-kind")
    @Outgoing("filtered-message")
    @Broadcast
    public PublisherBuilder<JsonObject> filterBySensor(JsonObject message) {
        String[] splittedMessage = message.getString("topic").split("/");
        String areaName = splittedMessage[0];
        String kindName = splittedMessage[1];
        String sensorName = splittedMessage[2];
        return ReactiveStreams.of(message).filter(s ->
            Optional.ofNullable(consulAuthorizedTopics.get())
                    .map(str -> new JsonObject(str))
                    .map(config -> config.getJsonObject(areaName))
                    .map(area -> area.getJsonArray(kindName))
                    .map(sensors -> sensors.contains(sensorName))
                    .get()
        );
    }

    @Incoming("filtered-message")
    @Outgoing("thermal-stream")
    @Broadcast
    public String publishRoundedValue(JsonObject message) {
        String[] splittedMessage = message.getString("topic").split("/");
        String areaName = splittedMessage[0];
        String kindName = splittedMessage[1];
        String sensorName = splittedMessage[2];
        int temp = (int)Math.round(message.getDouble("value"));
        return new JsonObject()
                .put("area", areaName)
                .put("kind", kindName)
                .put("sensor", sensorName)
                .put("value", temp)
                .toString();
    }

    @Incoming("filtered-message")
    public void saveMetric(JsonObject message) {
        String[] splittedMessage = message.getString("topic").split("/");
        String kindName = splittedMessage[1];
        String sensorName = splittedMessage[2];
        int temp = (int)Math.round(message.getDouble("value"));
        metricsService.add(new JsonObject()
                .put("kind", kindName)
                .put("sensor", sensorName)
                .put("value", temp));
    }
}
