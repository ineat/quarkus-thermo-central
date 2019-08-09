package com.ineat.quarkus.poc;

import io.quarkus.mongodb.ReactiveMongoClient;
import io.quarkus.mongodb.ReactiveMongoCollection;
import io.vertx.core.json.JsonObject;
import org.bson.Document;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class MetricsService {
    @Inject
    ReactiveMongoClient mongoClient;

    public CompletionStage<Void> create(JsonObject metric) {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        String date = formatter.format(calendar.getTime());

        Document searchDocument = new Document()
            .append("date", date)
            .append("area", metric.getString("area"))
            .append("type", metric.getString("kind"))
            .append("sensor", metric.getString("sensor"));

        CompletionStage<Void> stage = getCollection()
            .find(searchDocument, Document.class)
            .findFirst()
            .run()
            .thenAccept(maybeDoc -> {
               if(maybeDoc.isPresent()) {
                   Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                   Document value = new Document()
                           .append("val", metric.getInteger("value"))
                           .append("timestamp", timestamp.getTime());
                   Document foundDocument = maybeDoc.get();
                   List<Document> values = (List<Document>)foundDocument.get("values");
                   values.add(value);
                   Document updateDocument = new Document("$set", new Document("values", values));
                   getCollection().updateOne(searchDocument, updateDocument);

                } else {
                   Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                   List<Document> values = new ArrayList<>();
                   Document value = new Document();
                   value.put("val", metric.getInteger("value"));
                   value.put("timestamp", timestamp.getTime());
                   values.add(new Document(value));
                   Document document = new Document()
                           .append("date", date)
                           .append("area", metric.getString("area"))
                           .append("type", metric.getString("kind"))
                           .append("sensor", metric.getString("sensor"))
                           .append("values", values);
                   getCollection().insertOne(document);
               }
            });
        return stage;
    }

    private ReactiveMongoCollection<Document> getCollection(){
        return mongoClient.getDatabase("poc-quarkus-db").getCollection("poc-quarkus-metrics");
    }

}
