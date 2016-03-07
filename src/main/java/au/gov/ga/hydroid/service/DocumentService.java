package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.model.Document;

import java.util.List;

public interface DocumentService {
   List<Document> findAll();
   Document findByUrn(String urn);
   Document findByOrigin(String origin);
   void create(Document document);
   void deleteByUrn(String urn);
   void update(Document document);
   void clearAll();
}
