package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.EnhancementStatus;

import java.util.List;

/**
 * Created by u24529 on 4/02/2016.
 */
public interface DocumentService {
   
   List<Document> findAll();
   Document findByUrn(String urn);
   Document findByOrigin(String origin);
   Document findBySha1Hash(String sha1Hash);
   List<Document> findByStatus(EnhancementStatus status);
   void create(Document document);
   void deleteByUrn(String urn);
   void update(Document document);
   void clearAll();

   void createImageMetadata(String origin, String metadata);
   String readImageMetadata(String origin);
   void updateImageMetadata(String origin, String metadata);
   
}
