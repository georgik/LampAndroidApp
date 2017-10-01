# Lamp Control

Android application for controlling Lamp Relay connected to ESP8266

More information about the project:

- http://georgik.rocks/category/iot/

# Related project

- LampESP - https://github.com/georgik/LampESP - code for ESP8266 which can communicate with this app.

#Version 0.2

Communication is based on MQTT (Mosquitto server).

# Version 0.1

Direct communication based on GET API (local network only).
See branch v0.1.

# Customize application

Values of server and rooms are hardcoded. You can change them to suit
yout configuration.

- MainActivity.serverUri - set to your MQTT server
- LampMqttService.subscriptionTopic - MQTT topic where state of relay is being broadcasted
- LampMqttService.publishTopic - MQTT topic where client can send request to change state of relay

