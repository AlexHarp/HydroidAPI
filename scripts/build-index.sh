# init indexing tool
java -jar -Xmx1g -XX:MaxPermSize=256M tools/org.apache.stanbol.entityhub.indexing.genericrdf-0.12.1-SNAPSHOT.jar init
cp pp_project_gapublicvocabssandbox.rdf indexing/resources/rdfdata/pp_project_gapublicvocabssandbox.rdf
rm indexing/config/indexing.properties
cp indexing.properties indexing/config/indexing.properties
java -jar -Xmx1g -XX:MaxPermSize=256M tools/org.apache.stanbol.entityhub.indexing.genericrdf-0.12.1-SNAPSHOT.jar index
cp indexing/dist/GA.solrindex.zip staging/
cp indexing/dist/org.apache.stanbol.data.site.GA-1.0.0.jar staging/
cp scripts/reset_stanbol.sh staging/
cp appspec.yml staging/
cd staging
zip -r hydroid.zip *