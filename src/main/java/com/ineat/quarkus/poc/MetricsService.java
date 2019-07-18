package com.ineat.quarkus.poc;

import io.netty.util.internal.StringUtil;
import io.quarkus.mongodb.ReactiveMongoClient;
import io.quarkus.mongodb.ReactiveMongoCollection;
import io.vertx.core.json.JsonObject;
import org.bson.Document;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class MetricsService {
    @Inject
    ReactiveMongoClient mongoClient;

    public CompletionStage<Void> add(JsonObject metric) {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH");

        Calendar calendar = Calendar.getInstance();
        String minutes = String.valueOf(calendar.getTime().getMinutes());
        Date now = calendar.getTime();
        String timestamp = StringUtil.EMPTY_STRING;
        try {
            timestamp = String.valueOf(formatter.parse(formatter.format(now)).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Document metricDocument = new Document()
                .append(minutes, metric.getInteger("value"));

        Document sensorDocument = new Document()
                .append(metric.getString("sensor"), metricDocument);

        Document document = new Document()
                .append("timestamp_hour", timestamp)
                .append("type", metric.getString("kind"))
                .append("values", sensorDocument);

        return getCollection().insertOne(document);
    }

    private ReactiveMongoCollection<Document> getCollection(){
        return mongoClient.getDatabase("poc-quarkus-db").getCollection("poc-quarkus-metrics");
    }

}
