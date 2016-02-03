package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.service.EnhancerService;
import au.gov.ga.hydroid.service.SolrClient;
import au.gov.ga.hydroid.service.StanbolClient;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import org.openrdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by u24529 on 3/02/2016.
 */
@Service
public class EnhancerServiceImpl implements EnhancerService {

   private static final String[] VALID_PREDICATES = {"about", "title", "subject",
         "content", "concept", "label", "created", "created", "extracted-from"
   };

   @Autowired
   private StanbolClient stanbolClient;

   @Autowired
   private SolrClient solrClient;

   @Override
   public void enhance(String content) throws Exception {

      // Send content to Stanbol for enhancement
      List<Statement> rdfDocument = stanbolClient.enhance("hydroid", content, StanbolMediaTypes.RDFXML);
      if (rdfDocument != null) {
         String predicate = null;
         Properties properties = null;
         for (Statement statement : rdfDocument) {
            predicate = statement.getPredicate().getLocalName().toLowerCase();
            if (Arrays.binarySearch(VALID_PREDICATES, predicate) >= 0) {
               properties.put(predicate, "dummy");
            }
         }

         properties.propertyNames();

         // Add enhanced document to Solr
         //solrClient.addDocument("hydroid", properties);

      }

   }

}
