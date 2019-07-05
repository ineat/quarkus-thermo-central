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
import javax.inject.Provider;
import java.util.Optional;

@ApplicationScoped
public class ThermalProcessor {

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
        return msg;
    }

    @Incoming("converted-message")
    @Outgoing("filtered-message")
    @Broadcast
    public PublisherBuilder<Double> filterByTopic(JsonObject message) {
        return ReactiveStreams.of(message)
                .filter(s -> {
                    String[] splittedMessage = message.getString("topic").split("/");
                    String groupName = splittedMessage[0];
                    String sensorName = splittedMessage[1];
                    JsonObject config = new JsonObject(consulAuthorizedTopics.get());
                    return Optional.ofNullable(config.getJsonArray(groupName))
                            .filter(grp -> grp.contains(sensorName))
                            .isPresent();
                })
                .map(msg -> message.getDouble("value"));
    }

    @Incoming("filtered-message")
    @Outgoing("thermal-stream")
    @Broadcast
    public int publishRoundedValue(Double value) {
        int temp = (int)Math.round(value);
        System.out.println("Receiving temp: " + temp);
        return temp;
    }
}
