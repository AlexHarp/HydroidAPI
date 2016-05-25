# setup java and tomcat
sudo yum install -y java-1.8.0-openjdk.x86_64
sudo yum install -y tomcat8
sudo update-alternatives --set java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java

# install jena
cd /home/ec2-user
mkdir software
cd software
aws s3 cp s3://hydroid/releases/apache-jena-fuseki-2.3.1.zip /home/ec2-user/software/apache-jena-fuseki-2.3.1.zip
unzip apache-jena-fuseki-2.3.1.zip
cd apache-jena-fuseki-2.3.1
sudo cp fuseki.war /usr/share/tomcat8/webapps/fuseki.war

# install solr
cd /home/ec2-user/software
aws s3 cp s3://hydroid/releases/solr-5.4.1.zip /home/ec2-user/software/solr-5.4.1.zip
unzip solr-5.4.1.zip
aws s3 cp s3://hydroid/solr-cores/hydroid.zip /home/ec2-user/software/solr-5.4.1/server/solr/hydroid.zip
cd /home/ec2-user/software/solr-5.4.1/server/solr
unzip hydroid.zip
cd /home/ec2-user/software/solr-5.4.1
./bin/solr restart


# install stanbol
sudo aws s3 cp s3://hydroid/stanbol/stanbol.war /usr/share/tomcat8/webapps/stanbol.war
sudo service tomcat8 start

