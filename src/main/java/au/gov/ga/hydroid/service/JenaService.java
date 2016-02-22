package au.gov.ga.hydroid.service;

import com.hp.hpl.jena.rdf.model.Statement;

import java.util.List;

/**
 * Created by Layoric on 16/02/2016.
 */
public interface JenaService {

   public void storeRdfDefault(String rdfInput, String baseRdfUrl);
   public void storeRdf(String rdfId, String rdfInput, String baseRdfUrl);
   public List<Statement> parseRdf(String rdfInput, String baseRdfUrl) throws Exception;
   public void deleteRdfDefault();
   public void deleteRdf(String rdfId);
   public List<Statement> readRdf(String rdfId);

}
