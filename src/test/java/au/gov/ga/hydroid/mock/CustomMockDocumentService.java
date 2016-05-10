package au.gov.ga.hydroid.mock;

import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.EnhancementStatus;
import au.gov.ga.hydroid.service.DocumentService;

import java.util.ArrayList;
import java.util.List;

public class CustomMockDocumentService implements DocumentService {
    final List<Document> all = new ArrayList<Document>();

    public CustomMockDocumentService() {
        for (int i = 0; i < 5; i++) {
            Document doc = new Document();
            doc.setUrn("urn" + i);
            doc.setId(i);
            doc.setTitle("Title");
            doc.setOrigin("hydroid:input/urn" + i);
            all.add(doc);
        }
    }

    @Override
    public List<Document> findAll() {
        return all;
    }

    @Override
    public Document findByUrn(String urn) {
        for (Document doc : all) {
            if (doc.getUrn().equals(urn))
                return doc;
        }
        return null;
    }

   @Override
   public Document findByOrigin(String origin) {
      return null;
   }

   @Override
   public Document findBySha1Hash(String sha1Hash) {
      return null;
   }

   @Override
   public List<Document> findByStatus(EnhancementStatus status) {
      return null;
   }

   @Override
    public void create(Document document) {

    }

   @Override
   public void deleteByUrn(String urn) {

   }

   @Override
   public void update(Document document) {

   }

   @Override
   public void clearAll() {
      
   }

   @Override
   public void createImageMetadata(String origin, String metadata) {

   }

   @Override
   public String readImageMetadata(String origin) {
      return null;
   }

   @Override
   public void updateImageMetadata(String origin, String metadata) {

   }

}
