#!/bin/bash
sudo yum install -y java-1.7.0-openjdk.x86_64
sudo yum install -y tomcat7
update-alternatives --set java /usr/lib/jvm/jre-1.7.0-openjdk.x86_64/bin/java
aws s3 cp s3://hydroid/stanbol/stanbol.war /usr/share/tomcat7/webapps/stanbol.war
sudo service tomcat7 start
