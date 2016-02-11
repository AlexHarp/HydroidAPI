package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.model.Document;

import java.util.List;

/**
 * Created by u24529 on 4/02/2016.
 */
public interface DocumentService {

   public List<Document> findAll();
   public Document findByUrn(String urn);
   public void create(Document document);
   public void deleteByUrn(String urn);

}
