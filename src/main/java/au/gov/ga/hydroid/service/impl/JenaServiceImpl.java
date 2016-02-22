package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.JenaService;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Service
public class JenaServiceImpl implements JenaService {

   @Autowired
   private HydroidConfiguration configuration;

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
      if (graphUri == null) {
         accessor.putModel(model);
      } else {
         accessor.putModel(graphUri, model);
      }
   }

   @Override
   public List<Statement> parseRdf(String rdfInput, String baseRdfUrl) throws Exception {
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
