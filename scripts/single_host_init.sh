# setup java and tomcat
sudo yum install -y java-1.8.0-openjdk.x86_64
sudo yum install -y tomcat8
sudo update-alternatives --set java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java

yum -y update
yum install -y ruby
yum install -y aws-cli
cd /home/ec2-user
aws s3 cp s3://aws-codedeploy-ap-southeast-2/latest/install . --region ap-southeast-2
chmod +x ./install
./install auto

# install jena
cd /home/ec2-user
mkdir software
cd software
aws s3 cp s3://hydroid/releases/apache-jena-fuseki-2.3.1.zip /home/ec2-user/software/apache-jena-fuseki-2.3.1.zip
unzip apache-jena-fuseki-2.3.1.zip
cd apache-jena-fuseki-2.3.1
sudo cp fuseki.war /usr/share/tomcat8/webapps/fuseki.war
sudo mkdir /etc/fuseki
sudo chown -R :tomcat /etc/fuseki
sudo chmod -R g+w /etc/fuseki

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

sudo aws s3 cp s3://hydroid/releases/httpd.conf /etc/httpd/conf/httpd.conf
sudo service httpd restart

# Fuseki config
sudo aws s3 cp s3://hydroid/releases/shiro.ini /etc/fuseki/shiro.ini

curl 'http://localhost:8080/fuseki/$/datasets' -H 'Pragma: no-cache' -H 'Accept-Encoding: gzip, deflate' -H 'Content-Type: application/x-www-form-urlencoded; charset=UTF-8' -H 'Accept: */*' --data 'dbName=hydroid&dbType=tdb' --compressed
