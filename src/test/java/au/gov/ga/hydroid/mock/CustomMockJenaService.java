package au.gov.ga.hydroid.mock;

import au.gov.ga.hydroid.service.JenaService;
import org.apache.jena.rdf.model.Statement;

import java.util.List;

/**
 * Created by u24529 on 6/04/2016.
 */
public class CustomMockJenaService implements JenaService {

   @Override
   public void storeRdfDefault(String rdfInput, String baseRdfUrl) {

   }

   @Override
   public void storeRdf(String rdfId, String rdfInput, String baseRdfUrl) {

   }

   @Override
   public List<Statement> parseRdf(String rdfInput, String baseRdfUrl) {
      return null;
   }

   @Override
   public void deleteRdfDefault() {

   }

   @Override
   public void deleteRdf(String rdfId) {

   }

   @Override
   public List<Statement> readRdf(String rdfId) {
      return null;
   }

}
