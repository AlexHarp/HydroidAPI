package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.service.SolrClient;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by u24529 on 3/02/2016.
 */
public class SolrClientImpl implements SolrClient {

   private SolrInputDocument buildDocument(Properties properties) {
      SolrInputDocument document = new SolrInputDocument();
      if (properties != null) {
         String propertyName = null;
         Enumeration<String> propertyNames = (Enumeration<String>) properties.propertyNames();
         while (propertyNames.hasMoreElements()) {
            propertyName = propertyNames.nextElement();
            document.addField(propertyName, properties.get(propertyName));
         }
      }
      return document;
   }

   @Override
   public void addDocument(String collectionName, Properties properties) throws Exception {
      SolrServer server = new HttpSolrServer("http://127.0.0.1:8983/solr/" + collectionName);
      SolrInputDocument document = buildDocument(properties);
      server.add(document);
      server.commit();
   }

}
