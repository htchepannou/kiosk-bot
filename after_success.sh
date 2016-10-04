#!/bin/bash

mkdir target/deploy
cp target/kiosk-bot-1.0.jar target/deploy/kiosk-bot.jar

mkdir target/deploy/install
cp src/install/* target/deploy/install/.
