package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.model.Document;

import java.util.List;

/**
 * Created by u24529 on 4/02/2016.
 */
public interface DocumentService {

   public List<Document> findAll();
   public Document findByUrn(String urn);
   public Document findByOrigin(String origin);
   public void create(Document document);
   public void deleteByUrn(String urn);
   public void update(Document document);
   public void clearAll();

   public void createImageMetadata(String origin, String metadata);
   public String readImageMetadata(String origin);
   public void updateImageMetadata(String origin, String metadata);

}
