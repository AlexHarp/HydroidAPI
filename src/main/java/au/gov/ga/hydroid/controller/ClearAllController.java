package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.*;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

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

   @RequestMapping(value = "/all", method = {RequestMethod.GET})
   public @ResponseBody
   String resetAll() throws Exception {
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
   }
}
