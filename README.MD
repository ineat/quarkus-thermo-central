# Requierements

Before start working on this project, you need install [Consul](https://www.consul.io/) and [Mosquitto](https://mosquitto.org/).

## Init stack with  Docker
### Consul

```
docker run -d -p 8500:8500 consul agent -dev -client=0.0.0.0 -bind=0.0.0.0
```

After container is launched, add a new config named __"topics"__ in __key/value__ page. Add the following content to this new config :

```json
{
  "thermal": ["sensor1", "sensor2", "sensor4"],
  "particule": []
}
```

### Mosquitto

```
docker run -d -p 1883:1883 -p 9001:9001 eclipse-mosquitto
```

## Init stack with Compose
In project root launch :

```
docker-compose up
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