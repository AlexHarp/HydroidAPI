# init indexing tool
java -jar -Xmx1g -XX:MaxPermSize=256M tools/org.apache.stanbol.entityhub.indexing.genericrdf-0.12.1-SNAPSHOT.jar init
cp pp_project_gapublicvocabssandbox.rdf indexing/resources/rdfdata/pp_project_gapublicvocabssandbox.rdf
rm indexing/config/indexing.properties
cp indexing.properties indexing/config/indexing.properties
# The below command crashes on first try, no source available to fix, also doesn't exit.. timeout and then rerun.
timeout 20s java -jar -Xmx1g -XX:MaxPermSize=256M tools/org.apache.stanbol.entityhub.indexing.genericrdf-0.12.1-SNAPSHOT.jar index
java -jar -Xmx1g -XX:MaxPermSize=256M tools/org.apache.stanbol.entityhub.indexing.genericrdf-0.12.1-SNAPSHOT.jar index
cp scripts/reset_stanbol.sh indexing/dist/reset_stanbol.sh
cp scripts/start_server.sh indexing/dist/start_server.sh
cp scripts/stop_server.sh indexing/dist/stop_server.sh
aws s3 cp s3://hydroid/vocabulary/hydroid.sh indexing/dist/hydroid.sh
cp appspec.yml indexing/dist/
cd indexing/dist
zip -r hydroid.zip *