package com.ineat.quarkus.poc;

import com.mongodb.client.model.Filters;
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
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class MetricsService {
    @Inject
    ReactiveMongoClient mongoClient;

    public CompletionStage<Void> add(JsonObject metric) {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH");

        Calendar calendar = Calendar.getInstance();
        String minutes = String.valueOf(calendar.getTime().getMinutes());
        Date now = calendar.getTime();
        CompletionStage<Void> stage = null;
        try {
            final String timestamp = String.valueOf(formatter.parse(formatter.format(now)).getTime());

            Document searchDocument = new Document()
                    .append("timestamp_hour", timestamp)
                    .append("area", metric.getString("area"))
                    .append("type", metric.getString("kind"));


            stage = getCollection()
                    .find(searchDocument, Document.class)
                    .findFirst()
                    .run()
                    .thenAccept(maybeDoc -> {
                        if (maybeDoc.isPresent()) {
                            Document foundDocument = maybeDoc.get();
                            Document foundValues = foundDocument.get("values", Document.class);
                            Document newDocument = new Document()
                                    .append("timestamp_hour", timestamp)
                                    .append("area", metric.getString("area"))
                                    .append("type", metric.getString("kind"));

                            Document newValues = cloneDocument(foundValues);
                            Document newSensor;
                            if (newValues.containsKey(metric.getString("sensor"))) {
                                newSensor = newValues.get(metric.getString("sensor"), Document.class);
                            } else {
                                newSensor = newDocument;
                            }
                            newSensor.put(minutes, metric.getInteger("value"));
                            newValues.put(metric.getString("sensor"), newSensor);
                            newDocument.put("values", newValues);
                            getCollection().updateOne(Filters.eq(searchDocument), newDocument);

                        } else {
                            Document metricDocument = new Document()
                                    .append(minutes, metric.getInteger("value"));

                            Document sensorDocument = new Document()
                                    .append(metric.getString("sensor"), metricDocument);
                            Document document = new Document()
                                    .append("timestamp_hour", timestamp)
                                    .append("area", metric.getString("area"))
                                    .append("type", metric.getString("kind"))
                                    .append("values", sensorDocument);
                            getCollection().insertOne(document);
                        }
                    });

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return stage;
    }

    private Document cloneDocument(Document base) {
        Document newDocument = new Document();
        for (String key : base.keySet()) {
            if(base.get(key) instanceof Document){
                newDocument.put(key, cloneDocument((Document)base.get(key)));
            } else {
                newDocument.put(key, base.get(key));
            }
        }
        return newDocument;
    }

    private ReactiveMongoCollection<Document> getCollection(){
        return mongoClient.getDatabase("poc-quarkus-db").getCollection("poc-quarkus-metrics");
    }

}
