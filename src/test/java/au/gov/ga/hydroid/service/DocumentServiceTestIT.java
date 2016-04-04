package au.gov.ga.hydroid.service;

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
      String text = "The polar bear (Ursus maritimus) is a carnivorous bear whose native range lies largely within the Arctic Circle, encompassing the Arctic Ocean, its surrounding seas and surrounding land masses. It is a large bear, approximately the same size as the omnivorous Kodiak bear (Ursus arctos middendorffi).[3] A boar (adult male) weighs around 350–700 kg (772–1,543 lb),[4] while a sow (adult female) is about half that size. Although it is the sister species of the brown bear,[5] it has evolved to occupy a narrower ecological niche, with many body characteristics adapted for cold temperatures, for moving across snow, ice, and open water, and for hunting seals, which make up most of its diet.[6] Although most polar bears are born on land, they spend most of their time on the sea ice. Their scientific name means \\\"maritime bear\\\", and derives from this fact. Polar bears hunt their preferred food of seals from the edge of sea ice, often living off fat reserves when no sea ice is present. Because of their dependence on the sea ice, polar bears are classified as marine mammals.[7]"
            + "\nBecause of expected habitat loss caused by climate change, the polar bear is classified as a vulnerable species, and at least three of the nineteen polar bear subpopulations are currently in decline.[8] For decades, large-scale hunting raised international concern for the future of the species but populations rebounded after controls and quotas began to take effect.[9] For thousands of years, the polar bear has been a key figure in the material, spiritual, and cultural life of Arctic indigenous peoples, and polar bears remain important in their cultures.";
      byte[] content = text.getBytes();
      long timestamp = System.currentTimeMillis();
      String origin = "origin:" + timestamp;
      String urn = "urn:" + System.currentTimeMillis();
      document.setOrigin(origin);
      document.setUrn(urn);
      document.setTitle("Title for (" + urn + ")");
      document.setType(DocumentType.DOCUMENT);
      document.setStatus(EnhancementStatus.SUCCESS);
      documentService.create(document);
   }

   @Test
   public void testFindByUrn() {
      Document document = documentService.findByUrn("urn:content-item-sha1-978b8d13b31cdb4b36534682b99d9614bfcb510f");
      Assert.assertNotNull(document);
   }

   @Test
   public void testFindByStatus() {
      List<Document> documents = documentService.findByStatus(EnhancementStatus.PENDING);
      Assert.assertNotNull(documents);
   }

}
