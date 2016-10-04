#!/bin/sh

#
# Usage: ./install.sh
#

source environment
source service-profile

#------------------------------
# Download
#------------------------------
echo "DOWNLOADING..."
rm -f $SERVICE_NAME-*.jar
wget https://s3-us-west-2.amazonaws.com/install.com.tchepannou/$SERVICE_NAME/$SERVICE_NAME.jar

#------------------------------
# Create user
#------------------------------
echo "CREATING USER: $SERVUCE_USER..."
id -u webapp &>/dev/null || useradd $SERVICE_USER

#------------------------------
# Install application
#------------------------------
echo "INSTALLING APPLICATION..."
if [ ! -d "/opt/$SERVICE_NAME" ]; then
  mkdir /opt/$SERVICE_NAME
fi
if [ ! -d "/opt/$SERVICE_NAME/log" ]; then
  mkdir /opt/$SERVICE_NAME/log
fi
if [ ! -d "/opt/$SERVICE_NAME/config" ]; then
  mkdir /opt/$SERVICE_NAME/config
fi

cp $SERVICE_NAME.jar /opt/$SERVICE_NAME/$SERVICE_NAME.jar
cp ./config/* /opt/$SERVICE_NAME/config
chown -R webapp:webapp /opt/$SERVICE_NAME

#------------------------------
# startup script
#------------------------------
echo "INSTALLING INITD SCRIPTS..."
cp etc/service-profile /etc/
cp ../../templates/$SERVICE_TEMPLATE/initd.sh /etc/init.d/$SERVICE_NAME
chmod +x /etc/init.d/$SERVICE_NAME

/sbin/chkconfig --add $SERVICE_NAME
/sbin/chkconfig $SERVICE_NAME on


#------------------------------
# restart
#------------------------------
echo "RESTARTING SERVICE..."
/etc/init.d/$SERVICE_NAME stop
/etc/init.d/$SERVICE_NAME start


#------------------------------
# healthcheck
#------------------------------


#------------------------------
# restore if necessary
#------------------------------


#------------------------------
# cleanup
#------------------------------
rm install.sh
