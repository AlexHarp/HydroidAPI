#!/bin/bash
sudo yum install -y java-1.8.0-openjdk.x86_64
sudo yum install -y tomcat8
cd /home/ec2-user
mkdir software
cd software
wget http://apache.mirror.digitalpacific.com.au/jena/binaries/apache-jena-fuseki-2.3.1.zip
unzip apache-jena-fuseki-2.3.1.zip
cd apache-jena-fuseki-2.3.1
cp fuseki.war /usr/share/tomcat8/webapps/fuseki.war
sudo service tomcat8 start
