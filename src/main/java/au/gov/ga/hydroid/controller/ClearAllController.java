package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.DocumentService;
import au.gov.ga.hydroid.utils.HydroidException;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by u31532 on 24/02/2016.
 */
@RestController
@RequestMapping("/reset")
public class ClearAllController {

   private Logger logger = LoggerFactory.getLogger(getClass());

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   private DocumentService documentService;

   @RequestMapping(value = "/all", method = {RequestMethod.POST})
   public @ResponseBody
   String resetAll() {
      try {
         logger.debug("Deleting from SOLR");
         SolrServer server = new HttpSolrServer(configuration.getSolrUrl() + "hydroid");
         server.deleteByQuery("*:*");
         server.commit();
         logger.debug("Deleting from SOLR - SUCCESS");
         logger.debug("Deleting from Jena");
         String serviceURI = configuration.getFusekiUrl();
         DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI);
         accessor.deleteDefault();
         logger.debug("Deleting from Jena - SUCCESS");
         logger.debug("Deleting from Postgres");
         documentService.clearAll();
         logger.debug("Deleting from Postgres - SUCCESS");
         return "Success";
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

}
