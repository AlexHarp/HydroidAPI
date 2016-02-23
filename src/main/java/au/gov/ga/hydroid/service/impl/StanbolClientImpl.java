package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.JenaService;
import au.gov.ga.hydroid.service.RestClient;
import au.gov.ga.hydroid.service.StanbolClient;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import org.apache.jena.rdf.model.Statement;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Created by u24529 on 3/02/2016.
 */
@Service
public class StanbolClientImpl implements StanbolClient {

   private Logger logger = LoggerFactory.getLogger(StanbolClientImpl.class);

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   private RestClient restClient;

   @Autowired
   private JenaService jenaService;

   @Override
   public String enhance(String chainName, String content, MediaType outputFormat) throws Exception {

      String result = null;

      final UriBuilder enhancerBuilder = UriBuilder.fromUri(configuration.getStanbolUrl());
      enhancerBuilder.path(chainName);

      InputStream isContent = IOUtils.toInputStream(content);

      final Entity<?> entity = Entity.entity(isContent, MediaType.TEXT_PLAIN_TYPE);
      final Response response = restClient.post(enhancerBuilder.build(), entity, outputFormat);

      try {
         final Response.StatusType statusInfo = response.getStatusInfo();
         switch (statusInfo.getFamily()) {
            case CLIENT_ERROR: {
               throw new Exception(String.format("An unknown client error occurred while enhancing content: [HTTP %d] %s",
                     statusInfo.getStatusCode(), statusInfo.getReasonPhrase()));
            }
            case SERVER_ERROR: {
               throw new Exception(String.format("An unknown server error occurred while enhancing content: [HTTP %d] %s",
                     statusInfo.getStatusCode(), statusInfo.getReasonPhrase()));
            }
            case SUCCESSFUL: {
               logger.debug("enhance - content has been successfully enhanced");
               result = response.readEntity(String.class);

               // todo remove this when Hydroid Dev is available
               //FileInputStream fis = new FileInputStream("c:\\Users\\u24529\\Downloads\\sample1.rdf");
               //rdfParser.parse(fis, "");

               break;
            }
            default: {
               String errorMessage = String.format("Received unknown response from server: [HTTP %d] %s",
                     statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
               throw new Exception(errorMessage);
            }
         }
      } finally {
         response.close();
      }

      return result;
   }

   @Override
   public Properties findAllPredicates(String chainName, String content, MediaType outputFormat) throws Exception {

      Properties allPredicates = new Properties();

      String enhancedText = enhance(chainName, content, StanbolMediaTypes.RDFXML);
      List<Statement> rdfDocument = jenaService.parseRdf(enhancedText, "");

      if (rdfDocument != null) {
         String predicate;
         for (Statement statement : rdfDocument) {
            predicate = statement.getPredicate().getLocalName().toLowerCase();
            if (allPredicates.getProperty(predicate) == null) {
               String objectValue = statement.getObject().isLiteral() ? statement.getObject().asLiteral().getString()
                     : statement.getObject().asResource().getURI();
               allPredicates.put(predicate, objectValue);
            }
         }
      }

      return allPredicates;
   }

}
