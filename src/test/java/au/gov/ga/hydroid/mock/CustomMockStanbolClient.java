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

   private static final String DEFAULT_CHAIN_RESPONSE = "<rdf:RDF\n" +
         "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
         "    xmlns:j.0=\"http://purl.org/dc/terms/\"\n" +
         "    xmlns:j.1=\"http://fise.iks-project.eu/ontology/\" > \n" +
         "  <rdf:Description rdf:about=\"urn:enhancement-ad2d13bb-94db-7194-c37a-ad52e36f56ea\">\n" +
         "    <j.1:confidence rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">0.9999965381438903</j.1:confidence>\n" +
         "    <j.1:extracted-from rdf:resource=\"urn:content-item-sha1-88c0fd7f3679ba90b1e1eba89ba3197c7a26af09\"/>\n" +
         "    <j.0:created rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2016-04-07T01:16:29.011Z</j.0:created>\n" +
         "    <j.0:creator rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">org.apache.stanbol.enhancer.engines.langdetect.LanguageDetectionEnhancementEngine</j.0:creator>\n" +
         "    <j.0:language>en</j.0:language>\n" +
         "    <j.0:type rdf:resource=\"http://purl.org/dc/terms/LinguisticSystem\"/>\n" +
         "    <rdf:type rdf:resource=\"http://fise.iks-project.eu/ontology/Enhancement\"/>\n" +
         "    <rdf:type rdf:resource=\"http://fise.iks-project.eu/ontology/TextAnnotation\"/>\n" +
         "  </rdf:Description>\n" +
         "</rdf:RDF>";

   @Override
   public String enhance(String chainName, String content, MediaType outputFormat) {
      if ("hydroid".equals(chainName)) {
         try (InputStream inputStream = getClass().getResourceAsStream("/testfiles/stanbol-response.xml")) {
            return new String(IOUtils.fromInputStreamToByteArray(inputStream));
         } catch (Exception e) {
            logger.error("enhance - Exception: ", e);
            return "";
         }
      } else {
         return DEFAULT_CHAIN_RESPONSE;
      }
   }

   @Override
   public Properties findAllPredicates(String enhancedText) {
      StanbolClient stanbolClient = new StanbolClientImpl();
      ReflectionTestUtils.setField(stanbolClient, "jenaService", new CustomMockJenaService());
      return stanbolClient.findAllPredicates(enhancedText);
   }

}
