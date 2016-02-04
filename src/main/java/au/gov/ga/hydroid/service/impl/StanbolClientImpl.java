package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.service.StanbolClient;
import au.gov.ga.hydroid.utils.RestClient;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by u24529 on 3/02/2016.
 */
@Service
public class StanbolClientImpl implements StanbolClient {

   public static final String STANBOL_ENHANCER_URL = "http://hydroid-dev-web-lb-1763223935.ap-southeast-2.elb.amazonaws.com/stanbol/enhancer/chain/";
   private Logger logger = LoggerFactory.getLogger(StanbolClientImpl.class);

   @Override
   public List<Statement> enhance(String chainName, String content, MediaType outputFormat) throws Exception {

      List<Statement> graph = new ArrayList<Statement>();

      final UriBuilder enhancerBuilder = UriBuilder.fromUri(STANBOL_ENHANCER_URL);
      enhancerBuilder.path(chainName);

      InputStream isContent = IOUtils.toInputStream(content);

      final Entity<?> entity = Entity.entity(isContent, MediaType.TEXT_PLAIN_TYPE);
      final Response response = RestClient.post(enhancerBuilder.build(), entity, outputFormat);

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
               String result = response.readEntity(String.class);

               // todo remove this when Hydroid Dev is available
               FileInputStream fis = new FileInputStream("c:\\Users\\u24529\\Downloads\\sample1.rdf");

               RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);
               rdfParser.setRDFHandler(new StatementCollector(graph));
               //rdfParser.parse(new ByteArrayInputStream(result.getBytes()), "");
               rdfParser.parse(fis, "");

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

      return graph;
   }

}
