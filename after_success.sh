#!/bin/bash

wget http://roboconf.net/resources/build/settings.xml
mvn clean deploy -q --settings settings.xml

mkdir target/deploy
cp target/kiosk-bot-1.0.jar target/deploy/kiosk-bot.jar
cp src/install/* target/deploy/.
