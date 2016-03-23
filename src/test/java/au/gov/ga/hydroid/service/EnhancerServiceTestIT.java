package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.DocumentDTO;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.utils.IOUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.tika.metadata.Metadata;
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
      String origin = "/testfiles/36_4_1175-1197_Buss_and_Clote.pdf";
      Metadata metadata = new Metadata();
      DocumentDTO document = new DocumentDTO();
      document.docType = DocumentType.DOCUMENT.name();
      document.content = IOUtils.parseFile(this.getClass().getResourceAsStream(origin));
      document.title = metadata.get("title");
      document.author = metadata.get("author") == null ? metadata.get("Author") : metadata.get("author");
      document.dateCreated = DateUtils.parseDate(metadata.get("Creation-Date"), new String[] {"yyyy-MM-dd'T'HH:mm:ss'Z'"});
      enhancerService.enhance(document);
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
      DocumentDTO document = new DocumentDTO();
      document.title = "Document Title Created: " + System.currentTimeMillis();
      document.docType = DocumentType.DOCUMENT.name();
      document.origin = "Pasted Content";
      document.content = "This enhancement should find Corals, Terrace and Bob Marley. But Bob Marley should be discarded.";
      Assert.assertTrue(enhancerService.enhance(document));
   }

   @Test
   public void testNotMatchedGAVocabs() {
      ReflectionTestUtils.setField(configuration, "stanbolChain", "default");
      ReflectionTestUtils.setField(configuration, "storeGAVocabsOnly", true);
      DocumentDTO document = new DocumentDTO();
      document.title = "Document Title Created: " + System.currentTimeMillis();
      document.docType = DocumentType.DOCUMENT.name();
      document.origin = "Pasted Content";
      document.content = "This enhancement should find Corals, Terrace and Bob Marley. But Bob Marley should be discarded.";
      Assert.assertFalse(enhancerService.enhance(document));
   }

   @Test
   public void testPDFMetadata() {
      String origin = "/testfiles/36_4_1175-1197_Buss_and_Clote.pdf";
      Metadata metadata = new Metadata();
      IOUtils.parseFile(this.getClass().getResourceAsStream(origin), metadata);
      Assert.assertTrue(metadata.size() > 0);
      Assert.assertEquals("Title", "Solving the Fisher-Wright and Coalescence Problems with a Discrete Markov Chain Analysis", metadata.get("title"));
      Assert.assertNotNull("Creation-Date", DateUtils.parseDate(metadata.get("Creation-Date"), new String[] {"yyyy-MM-dd'T'HH:mm:ss'Z'"}));
   }

   @Test
   public void testWordMetadata() {
      String origin = "/testfiles/whale.docx";
      Metadata metadata = new Metadata();
      IOUtils.parseFile(this.getClass().getResourceAsStream(origin), metadata);
      Assert.assertTrue(metadata.size() > 0);
      Assert.assertEquals("Title", "Whale text from Wikipedia", metadata.get("title"));
      Assert.assertEquals("Author", "Carneiro Elton", metadata.get("Author"));
      Assert.assertNotNull("Creation-Date", DateUtils.parseDate(metadata.get("Creation-Date"), new String[] {"yyyy-MM-dd'T'HH:mm:ss'Z'"}));
   }

}
