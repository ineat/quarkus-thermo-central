# Requierements

Before start working on this project, you need install [Consul](https://www.consul.io/) and [Mosquitto](https://mosquitto.org/).

## Init stack manually with Docker
### Consul

```
docker run -d -p 8500:8500 consul agent -dev -client=0.0.0.0 -bind=0.0.0.0
```

### Mosquitto

```
docker run -d -p 1883:1883 -p 9001:9001 eclipse-mosquitto
```

### MongoDb

```
docker run -d -e MONGO_INITDB_ROOT_USERNAME="admin-user" -e MONGO_INITDB_ROOT_PASSWORD="admin-password" -e MONGO_INITDB_DATABASE="poc-quarkus-db" -v ./db/mongo-init.js:/docker-entrypoint-initdb.d/mongo-init.js:ro -p 27017:27017 mongo
```

## Init stack automatically with Compose
In project root launch :

```
docker-compose up
```

## Init Consul configuration

After Consul container has been launched, add a new config named __"topics"__ in __key/value__ page. Add the following content to this new config :

```json
{ 
    "ilab" : { 
      "thermal" : ["sensor1", "sensor2", "sensor4"],
      "particule" : []
		},
  	"cds" : {}
}
```


# Run application

## Development mode

### Simulate sensor data

In development mode you can "simulate" sensor data with :

```
python mosquitto-sender.py sensor1
```

Random value between 20 and 40 will be sent periodically in Mosquitto on "thermal/sensor1" topic.

### Launch the Quarkus application

Application can be launch with this command : 

```
mvn compile quarkus:dev 
```

After it's started, go to http://localhost:8080/sensors.html to show sensors values (refresh dynamically).