package au.gov.ga.hydroid;

import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.service.EnhancerService;
import au.gov.ga.hydroid.utils.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

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

   @Test
   public void testEnhanceImages() {
      enhancerService.enhanceImages();
   }

   @Test
   public void testMatchedGAVocabs() {
      ReflectionTestUtils.setField(configuration, "stanbolChain", "hydroid");
      ReflectionTestUtils.setField(configuration, "storeGAVocabsOnly", true);
      String randomText = "Created: " + System.currentTimeMillis();
      String text = "This enhancement should find Corals, Terrace and Bob Marley. But Bob Marley should be discarded.";
      Assert.assertTrue(enhancerService.enhance("Document Title" + randomText, text, DocumentType.DOCUMENT.name(), "Pasted Content"));
   }

   @Test
   public void testNotMatchedGAVocabs() {
      ReflectionTestUtils.setField(configuration, "stanbolChain", "default");
      ReflectionTestUtils.setField(configuration, "storeGAVocabsOnly", true);
      String randomText = "Created: " + System.currentTimeMillis();
      String text = "This enhancement should find Corals, Terrace and Bob Marley. But Bob Marley should be discarded.";
      Assert.assertFalse(enhancerService.enhance("Document Title" + randomText, text, DocumentType.DOCUMENT.name(), "Pasted Content"));
   }

}
