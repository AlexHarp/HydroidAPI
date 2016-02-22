package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.JenaService;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Service
public class JenaServiceImpl implements JenaService {

   @Autowired
   private HydroidConfiguration configuration;

   private void setNsPrefix(Model model) {
      model.setNsPrefix(OWL.class.getSimpleName().toLowerCase(), OWL.getURI());
      model.setNsPrefix(RDF.class.getSimpleName().toLowerCase(), RDF.getURI());
      model.setNsPrefix(RDFS.class.getSimpleName().toLowerCase(), RDFS.getURI());
      model.setNsPrefix(FOAF.class.getSimpleName().toLowerCase(), FOAF.getURI());
      model.setNsPrefix(DC.class.getSimpleName().toLowerCase(), DC.getURI());
      model.setNsPrefix(DCTerms.class.getSimpleName().toLowerCase(), DCTerms.getURI());
      model.setNsPrefix("stanbol", "http://stanbol.apache.org/ontology/entityhub/entityhub#");
      model.setNsPrefix("fise-iks", "http://fise.iks-project.eu/ontology/");
   }

   @Override
   public void storeRdf(String rdfId, String rdfInput, String baseRdfUrl) {
      String serviceURI = configuration.getFusekiUrl();
      DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI);
      Model model = ModelFactory.createDefaultModel();
      InputStream is = new ByteArrayInputStream(rdfInput.getBytes());
      model.read(is, baseRdfUrl);
      setNsPrefix(model);
      accessor.putModel(rdfId, model);
   }

   @Override
   public List<Statement> parseRdf(String rdfInput, String baseRdfUrl) throws Exception {
      Model model = ModelFactory.createDefaultModel();
      InputStream is = new ByteArrayInputStream(rdfInput.getBytes());
      model.read(is, baseRdfUrl);
      return model.listStatements().toList();
   }

   @Override
   public void deleteRdf(String rdfId) {
      String serviceURI = configuration.getFusekiUrl();
      DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI);
      accessor.deleteModel(rdfId);
   }

   @Override
   public List<Statement> readRdf(String rdfId) {
      String serviceURI = configuration.getFusekiUrl();
      DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI);
      Model model = accessor.getModel(rdfId);
      if (model == null) {
         return null;
      }
      return model.listStatements().toList();
   }

}
