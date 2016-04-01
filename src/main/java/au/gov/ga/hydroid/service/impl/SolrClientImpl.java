package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.SolrClient;
import au.gov.ga.hydroid.utils.HydroidException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Enumeration;
import java.util.List;
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
         Object propertyValue = null;
         Enumeration<String> propertyNames = (Enumeration<String>) properties.propertyNames();
         while (propertyNames.hasMoreElements()) {
            propertyName = propertyNames.nextElement();
            propertyValue = properties.get(propertyName);
            if (propertyValue instanceof List) {
               for (String multiValueItem : (List<String>) propertyValue) {
                  document.addField(propertyName, multiValueItem);
               }
            } else {
               document.addField(propertyName, propertyValue);
            }
         }
      }
      return document;
   }

   @Override
   public void addDocument(String collectionName, Properties properties) {
      SolrServer server = new HttpSolrServer(configuration.getSolrUrl() + collectionName);
      SolrInputDocument document = buildDocument(properties);
      try {
         server.add(document);
         server.commit();
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Override
   public void deleteDocument(String collectionName, String id) {
      SolrServer server = new HttpSolrServer(configuration.getSolrUrl() + collectionName);
      try {
         server.deleteById(id);
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

}
