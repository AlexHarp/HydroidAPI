package au.gov.ga.hydroid.mock;

import au.gov.ga.hydroid.service.StanbolClient;
import au.gov.ga.hydroid.service.impl.StanbolClientImpl;
import au.gov.ga.hydroid.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by u24529 on 22/02/2016.
 */
public class CustomMockStanbolClient implements StanbolClient {

   private static final Logger logger = LoggerFactory.getLogger(CustomMockStanbolClient.class);

   @Override
   public String enhance(String chainName, String content, MediaType outputFormat) {
      String responseFilePath;
      if ("hydroid".equals(chainName)) {
         responseFilePath = "/testfiles/stanbol-hydroid-response.xml";
      } else {
         responseFilePath = "/testfiles/stanbol-default-response.xml";
      }
      try (InputStream inputStream = getClass().getResourceAsStream(responseFilePath)) {
         return new String(IOUtils.fromInputStreamToByteArray(inputStream));
      } catch (Exception e) {
         logger.error("enhance - Exception: ", e);
         return "";
      }
   }

   @Override
   public Properties findAllPredicates(String enhancedText) {
      StanbolClient stanbolClient = new StanbolClientImpl();
      ReflectionTestUtils.setField(stanbolClient, "jenaService", new CustomMockJenaService());
      return stanbolClient.findAllPredicates(enhancedText);
   }

}
