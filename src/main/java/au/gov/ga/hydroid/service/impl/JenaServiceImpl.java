package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.JenaService;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.*;
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
   public void storeRdfDefault(String rdfInput, String baseRdfUrl) {
      storeRdf(null, rdfInput, baseRdfUrl);
   }

   @Override
   public void storeRdf(String graphUri, String rdfInput, String baseRdfUrl) {
      String serviceURI = configuration.getFusekiUrl();
      DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI);
      Model model = ModelFactory.createDefaultModel();
      InputStream is = new ByteArrayInputStream(rdfInput.getBytes());
      model.read(is, baseRdfUrl);
      setNsPrefix(model);
      if (graphUri == null) {
         accessor.add(model);
      } else {
         accessor.add(graphUri, model);
      }
   }

   @Override
   public List<Statement> parseRdf(String rdfInput, String baseRdfUrl) {
      Model model = ModelFactory.createDefaultModel();
      InputStream is = new ByteArrayInputStream(rdfInput.getBytes());
      model.read(is, baseRdfUrl);
      return model.listStatements().toList();
   }

   @Override
   public void deleteRdfDefault() {
      deleteRdf(null);
   }

   @Override
   public void deleteRdf(String graphUri) {
      String serviceURI = configuration.getFusekiUrl();
      DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI);
      if (graphUri == null) {
         accessor.deleteDefault();
      } else if (accessor.containsModel(graphUri)) {
         accessor.deleteModel(graphUri);
      }
   }

   @Override
   public List<Statement> readRdf(String graphUri) {
      String serviceURI = configuration.getFusekiUrl();
      DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI);
      Model model = accessor.getModel(graphUri);
      if (model == null) {
         return null;
      }
      return model.listStatements().toList();
   }

}
