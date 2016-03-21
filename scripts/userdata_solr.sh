#!/bin/bash
cd /home/ec2-user
mkdir software
cd software
wget https://www.apache.org/dist/lucene/solr/5.4.1/solr-5.4.1.zip
unzip solr-5.4.1.zip
aws s3 cp s3://hydroid/solr-cores/hydroid.zip /home/ec2-user/software/solr-5.4.1/server/solr/hydroid.zip
unzip hydroid.zip
cd /home/ec2-user/software/solr-5.4.1
./bin/solr restart