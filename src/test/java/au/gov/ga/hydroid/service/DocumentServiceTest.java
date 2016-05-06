package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.model.EnhancementStatus;
import au.gov.ga.hydroid.utils.HydroidException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Created by u24529 on 08/04/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@ActiveProfiles("dev")
public class DocumentServiceTest {

   @Autowired
   private DocumentService documentService;

   @Test
   public void testCreate() {
      Document document = new Document();
      String urn = "urn:test2";
      document.setOrigin("origin:test2");
      document.setUrn(urn);
      document.setTitle("Title for (" + urn + ")");
      document.setType(DocumentType.DOCUMENT);
      document.setStatus(EnhancementStatus.PENDING);
      documentService.create(document);
   }

   @Test
   public void testCreateWithError() {
      try {
         Document document = new Document();
         String urn = "urn:test3";
         document.setOrigin("origin:test1");
         document.setUrn(urn);
         document.setTitle("Title for (" + urn + ")");
         document.setType(DocumentType.DOCUMENT);
         document.setStatus(EnhancementStatus.PENDING);
         documentService.create(document);
      } catch (HydroidException e) {
         Assert.assertEquals("Unique index or primary key violation: \"DOCUMENTS_ORIGIN_IDX ON PUBLIC.DOCUMENTS(ORIGIN) VALUES ('origin:test1', 1)\"; SQL statement:\n" +
               "insert into documents (origin, urn, title, type, status, status_reason, process_date, parser_name) values (?, ?, ?, ?, ?, ?, ?, ?) [23505-191]",
               e.getMessage());
      }
   }

   @Test
   public void testFindAll() {
      List<Document> documents = documentService.findAll();
      Assert.assertNotNull(documents);
      Assert.assertTrue(!documents.isEmpty());
   }

   @Test
   public void testFindByUrn() {
      Document document = documentService.findByUrn("urn:test1");
      Assert.assertNotNull(document);
      Assert.assertEquals("urn:test1", document.getUrn());
   }

   @Test
   public void testFindByOrigin() {
      Document document = documentService.findByOrigin("origin:test1");
      Assert.assertNotNull(document);
      Assert.assertEquals("origin:test1", document.getOrigin());
   }

   @Test
   public void testFindByStatus() {
      List<Document> documents = documentService.findByStatus(EnhancementStatus.SUCCESS);
      Assert.assertNotNull(documents);
      Assert.assertEquals("urn:test1", documents.get(0).getUrn());
   }

   @Test
   @Transactional(readOnly = true)
   public void testDeleteByUrn() {
      documentService.deleteByUrn("urn:delete");
   }

   @Test
   @Transactional(readOnly = true)
   public void testUpdate() {
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
   @Transactional(readOnly = true)
   public void testClearAll() {
      documentService.clearAll();
      List<Document> documents = documentService.findAll();
      Assert.assertNotNull(documents);
      Assert.assertTrue(documents.isEmpty());
   }

   @Test
   public void testCreateImageMetadata() {
      documentService.createImageMetadata("origin:bear", "Animal, Mammal, Ursidae");
   }

   @Test
   public void testReadImageMetadata() {
      String metadata = documentService.readImageMetadata("origin:whale");
      Assert.assertNotNull(metadata);
      Assert.assertEquals("Whale, Mammal, Fish", metadata);
   }

   @Test
   public void testUpdateImageMetadata() {
      documentService.updateImageMetadata("origin:monkey", "Animal, Mammal, Primate, Monkey");
      String metadata = documentService.readImageMetadata("origin:monkey");
      Assert.assertNotNull(metadata);
      Assert.assertEquals("Animal, Mammal, Primate, Monkey", metadata);
   }

}
