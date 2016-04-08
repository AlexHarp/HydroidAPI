package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.model.EnhancementStatus;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;


/**
 * Created by u24529 on 08/04/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DocumentServiceTest {

   @Autowired
   private DocumentService documentService;

   @Test
   public void test1Create() {
      // create 1st doc
      Document document = new Document();
      String urn = "urn:test1";
      document.setOrigin("origin:test1");
      document.setUrn(urn);
      document.setTitle("Title for (" + urn + ")");
      document.setType(DocumentType.DOCUMENT);
      document.setStatus(EnhancementStatus.SUCCESS);
      documentService.create(document);
      // create 2st doc
      document = new Document();
      urn = "urn:test2";
      document.setOrigin("origin:test2");
      document.setUrn(urn);
      document.setTitle("Title for (" + urn + ")");
      document.setType(DocumentType.DOCUMENT);
      document.setStatus(EnhancementStatus.PENDING);
      documentService.create(document);
   }

   @Test
   public void test2FindAll() {
      List<Document> documents = documentService.findAll();
      Assert.assertNotNull(documents);
      Assert.assertEquals(2, documents.size());
   }

   @Test
   public void test3FindByUrn() {
      Document document = documentService.findByUrn("urn:test1");
      Assert.assertNotNull(document);
      Assert.assertEquals("urn:test1", document.getUrn());
   }

   @Test
   public void test4FindByOrigin() {
      Document document = documentService.findByOrigin("origin:test1");
      Assert.assertNotNull(document);
      Assert.assertEquals("origin:test1", document.getOrigin());
   }

   @Test
   public void test5FindByStatus() {
      List<Document> documents = documentService.findByStatus(EnhancementStatus.SUCCESS);
      Assert.assertNotNull(documents);
      Assert.assertEquals("urn:test1", documents.get(0).getUrn());
   }

   @Test
   public void test6Update() {
      Document document = documentService.findByUrn("urn:test1");
      Assert.assertNotNull(document);
      document.setUrn("urn:test1-updated");
      documentService.update(document);
      // read again to check new value
      document = documentService.findByUrn("urn:test1-updated");
      Assert.assertNotNull(document);
      Assert.assertEquals("urn:test1-updated", document.getUrn());
   }

   @Test
   public void test7ClearAll() {
      documentService.clearAll();
      List<Document> documents = documentService.findAll();
      Assert.assertNotNull(documents);
      Assert.assertTrue(documents.isEmpty());
   }

   /*
   void createImageMetadata(String origin, String metadata);
   String readImageMetadata(String origin);
   void updateImageMetadata(String origin, String metadata);
   */

}
