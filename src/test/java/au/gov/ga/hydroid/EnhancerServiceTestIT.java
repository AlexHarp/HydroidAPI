package au.gov.ga.hydroid;

import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.service.EnhancerService;
import au.gov.ga.hydroid.utils.IOUtils;
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
   public void testEnhance() {
      String randomText = "Created: " + System.currentTimeMillis();
      String origin = "/testfiles/36_4_1175-1197_Buss_and_Clote.pdf";
      String text = IOUtils.parseFile(this.getClass().getResourceAsStream(origin));

      enhancerService.enhance("Document Title" + randomText, text, DocumentType.DOCUMENT.name(), origin);
   }

   @Test
   public void testEnhanceDocuments() {
      enhancerService.enhanceDocuments();
   }

}
