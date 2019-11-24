#include "OneWire.h"
#include "DallasTemperature.h"
#include "WiFi.h"
#include <PubSubClient.h>

OneWire oneWire(27);
DallasTemperature tempSensor(&oneWire);
const char* ssid = "SSID-WIFI";
const char* password = "PASSWORD";
const char* mqtt_server = "IP-MQTT";
const char* clientName = "ilabthermalsensor1client";
const char* topicName = "ilab/thermal/sensor1";

WiFiClient espClient;
PubSubClient client(espClient);
char msg[20];

void setup(void)
{
  Serial.begin(115200);
  setupWifi();
  setupMqtt();
  tempSensor.begin();
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");

    if (client.connect(clientName)) {
      Serial.println("connected");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}

void loop(void)
{
  if (!client.connected()) {
    reconnect();
  }
  client.loop();
  tempSensor.requestTemperaturesByIndex(0);
  float value = tempSensor.getTempCByIndex(0);
  Serial.print("Temperature: ");
  Serial.print(value);
  Serial.println(" C");
  snprintf (msg, 20, "%lf", value);
  client.publish(topicName, msg);
  delay(2000);
}

void setupWifi() {
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.println("Connecting to WiFi..");
  }
  Serial.println("Connected to the WiFi network");
}

void setupMqtt() {
  client.setServer(mqtt_server, 1883);
}