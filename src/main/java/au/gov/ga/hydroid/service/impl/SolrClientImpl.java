package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.SolrClient;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by u24529 on 3/02/2016.
 */
@Service
public class SolrClientImpl implements SolrClient {

   @Autowired
   private HydroidConfiguration configuration;

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
      SolrServer server = new HttpSolrServer(configuration.getSolrUrl() + collectionName);
      SolrInputDocument document = buildDocument(properties);
      server.add(document);
      server.commit();
   }

}
