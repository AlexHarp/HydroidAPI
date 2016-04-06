package au.gov.ga.hydroid.mock;

import au.gov.ga.hydroid.service.SolrClient;

import java.util.Properties;

/**
 * Created by u24529 on 6/04/2016.
 */
public class CustomMockSolrClient implements SolrClient {

   @Override
   public void addDocument(String collectionName, Properties properties) {
   }

   @Override
   public void deleteDocument(String collectionName, String id) {
   }

   @Override
   public void deleteAll(String collectionName) {
   }

}
