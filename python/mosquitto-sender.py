import paho.mqtt.client as paho
import sys
import time
import random
broker="localhost"
port=1883
def on_publish(client,userdata,result):
    print(sys.argv[1] + " : Data published")
    pass
client= paho.Client("admin " + sys.argv[1])
client.on_publish = on_publish
client.connect(broker,port)
while (True):
    d=int(sys.argv[2])
    temp=random.randint(20,35)
    time.sleep(d)
    print(temp)
    ret= client.publish("ilab/thermal/"+sys.argv[1], temp)
print("Stopped...")