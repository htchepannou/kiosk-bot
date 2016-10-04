#!/bin/bash

source service-profile

echo "SERVICE_NAME=$SERVICE_NAME"
echo "SERVICE_TEMPLATE=$SERVICE_TEMPLATE"

chmod +x install.sh

./install.sh
