package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.DocumentService;
import au.gov.ga.hydroid.service.JenaService;
import au.gov.ga.hydroid.service.SolrClient;
import au.gov.ga.hydroid.utils.HydroidException;
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

   private static final Logger logger = LoggerFactory.getLogger(ClearAllController.class);

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   private SolrClient solrClient;

   @Autowired
   private JenaService jenaService;

   @Autowired
   private DocumentService documentService;

   @RequestMapping(value = "/all", method = {RequestMethod.POST})
   public @ResponseBody
   String resetAll() {
      try {
         logger.debug("Deleting from SOLR");
         solrClient.deleteAll(configuration.getSolrCollection());
         logger.debug("Deleting from SOLR - SUCCESS");
         logger.debug("Deleting from Jena");
         jenaService.deleteRdfDefault();
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
