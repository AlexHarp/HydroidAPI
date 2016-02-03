#!/bin/sh
sudo yum install -y java-1.7.0-openjdk.x86_64 java-1.7.0-openjdk-devel.x86_64
sudo yum install -y tomcat tomcat7-webapps tomcat7-docs-webapp tomcat7-admin-webapps
update-alternatives --set java /usr/lib/jvm/jre-1.7.0-openjdk.x86_64/bin/java
sudo service tomcat7 start
aws s3 cp s3://hydroid/stanbol/stanbol.war /usr/share/tomcat8/webapps/stanbol.war