# init indexing tool
java -jar -Xmx1g -XX:MaxPermSize=256M /tools/org.apache.stanbol.entityhub.indexing.genericrdf-0.12.1-SNAPSHOT.jar init
cp pp_project_gapublicvocabssandbox.rdf indexing/resources/rdfdata/pp_project_gapublicvocabssandbox.rdf
rm indexing/config/indexing.properties
cp indexing.properties indexing/config/indexing.properties
java -jar -Xmx1g -XX:MaxPermSize=256M /tools/org.apache.stanbol.entityhub.indexing.genericrdf-0.12.1-SNAPSHOT.jar index
# Run it twice because first time fails on method not found...
java -jar -Xmx1g -XX:MaxPermSize=256M /tools/org.apache.stanbol.entityhub.indexing.genericrdf-0.12.1-SNAPSHOT.jar index
