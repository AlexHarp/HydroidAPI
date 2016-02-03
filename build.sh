# init indexing tool
java -jar /tools/org.apache.stanbol.entityhub.indexing.genericrdf-0.12.1-SNAPSHOT.jar init
cp pp_project_gapublicvocabssandbox.rdf indexing/resources/rdfdata/pp_project_gapublicvocabssandbox.rdf
rm indexing/config/indexing.properties
cp indexing.properties indexing/config/indexing.properties
java -jar /tools/org.apache.stanbol.entityhub.indexing.genericrdf-0.12.1-SNAPSHOT.jar index
java -jar /tools/org.apache.stanbol.entityhub.indexing.genericrdf-0.12.1-SNAPSHOT.jar index