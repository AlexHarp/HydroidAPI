package au.gov.ga.hydroid;

import au.gov.ga.hydroid.controller.FileIndexController;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.service.EnhancerService;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by u24529 on 3/02/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class EnhancerServiceTestIT {

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   private EnhancerService enhancerService;

   @Test
   public void testEnhance() throws Exception {
      String randomText = "Created: " + System.currentTimeMillis();
      String text = parseFile(this.getClass().getResourceAsStream("/testfiles/36_4_1175-1197_Buss_and_Clote.pdf"));

      enhancerService.enhance("Document Title" + randomText,
              text,
              DocumentType.DOCUMENT.name());
   }

   @Test
   public void testReindexDocument() throws Exception {
      enhancerService.reindexDocument("urn:content-item-sha1-88d676ed33e1b645fbd1e1a812f78b514ada8b16", false);
   }

   public static String parseFile(InputStream stream) throws IOException, SAXException, TikaException {
      AutoDetectParser parser = new AutoDetectParser();
      BodyContentHandler handler = new BodyContentHandler();
      Metadata metadata = new Metadata();
      parser.parse(stream, handler, metadata);
      return handler.toString();
   }

}
