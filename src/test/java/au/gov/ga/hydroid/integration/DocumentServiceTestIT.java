package au.gov.ga.hydroid.integration;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.model.EnhancementStatus;
import au.gov.ga.hydroid.service.DocumentService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;


/**
 * Created by u24529 on 3/02/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class DocumentServiceTestIT {

   @Autowired
   private DocumentService documentService;

   @Test
   public void testFindAll() {
      documentService.findAll();
   }

   @Test
   public void testCreate() {
      Document document = new Document();
      long timestamp = System.currentTimeMillis();
      String urn = "urn:" + System.currentTimeMillis();
      document.setOrigin("origin:" + timestamp);
      document.setUrn(urn);
      document.setTitle("Title for (" + urn + ")");
      document.setType(DocumentType.DOCUMENT);
      document.setStatus(EnhancementStatus.SUCCESS);
      documentService.create(document);
   }

   @Test
   public void testFindByUrn() {
      Document document = documentService.findByUrn("invalid:urn");
      Assert.assertNull(document);
   }

   @Test
   public void testFindByStatus() {
      List<Document> documents = documentService.findByStatus(EnhancementStatus.PENDING);
      Assert.assertNotNull(documents);
   }

}
