package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.service.*;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import com.hp.hpl.jena.rdf.model.*;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by u24529 on 3/02/2016.
 */
@Service
public class EnhancerServiceImpl implements EnhancerService {

   private Logger logger = LoggerFactory.getLogger(getClass());

   private static final String[] VALID_PREDICATES = {"title", "subject", "created", "extracted-from", "entity-reference", "entity-label"};
   private static final String GA_PUBLIC_VOCABS = "GAPublicVocabsSandbox";
   private static final String DOCUMENT_CREATOR = "Hydroid Enhancer App";

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   private StanbolClient stanbolClient;

   @Autowired
   private SolrClient solrClient;

   @Autowired
   private JenaService jenaService;

   @Autowired
   private DocumentService documentService;

   private Properties generateSolrDocument(List<Statement> rdfDocument, String content, String docType, String title) {
      String predicate = null;
      List<String> concepts = new ArrayList<>();
      List<String> labels = new ArrayList<>();
      Properties properties = new Properties();
      for (Statement statement : rdfDocument) {
         predicate = statement.getPredicate().getLocalName().toLowerCase();
         if (ArrayUtils.indexOf(VALID_PREDICATES, predicate) >= 0) {
            String objectValue = statement.getObject().isLiteral() ? statement.getObject().asLiteral().getString()
                  : statement.getObject().asResource().getURI();
            if (predicate.equalsIgnoreCase("extracted-from") && properties.getProperty("about") == null) {
               properties.put("about", objectValue);
            } else if (predicate.equalsIgnoreCase("entity-reference")) {
               // add new concept if not there yet
               if (!concepts.contains(objectValue)) {
                  concepts.add(objectValue);
               }
            } else if (predicate.equalsIgnoreCase("entity-label")) {
               // add new label if not there yet
               if (!labels.contains(objectValue)) {
                  labels.add(objectValue);
               }
            } else {
               properties.put(predicate, objectValue);
            }
         }
      }

      // If docType is provided we add it to the rdf document
      if (docType != null && !docType.isEmpty()) {

         // Add property:type to rdf (DOCUMENT, DATASET or MODEL)
         Resource subject = ResourceFactory.createResource(properties.getProperty("about"));
         Property property = ResourceFactory.createProperty("https://www.w3.org/TR/rdf-schema/#ch_type");
         Literal object = ResourceFactory.createPlainLiteral(docType);
         Statement statement = ResourceFactory.createStatement(subject, property, object);
         rdfDocument.add(statement);

         // Add property:label to the rdf (the document title)
         property = ResourceFactory.createProperty("https://www.w3.org/TR/rdf-schema/#ch_label");
         object = ResourceFactory.createPlainLiteral(title);
         statement = ResourceFactory.createStatement(subject, property, object);
         rdfDocument.add(statement);
      }

      String solrContent = content;
      if (solrContent.length() > 500) {
         solrContent = solrContent.substring(0, 500) + "...";
      }
      properties.put("content", solrContent);
      properties.put("label", labels);
      properties.put("concept", concepts);
      properties.put("docType", docType);
      properties.put("creator", DOCUMENT_CREATOR);

      return properties;
   }

   @Override
   public void enhance(String title, String content, String docType) throws Exception {

      String urn = null;
      Properties properties = null;

      try {

         // Send content to Stanbol for enhancement
         logger.info("enhance - about to post to stanbol server");
         String enhancedText = stanbolClient.enhance(configuration.getStanbolChain(), content, StanbolMediaTypes.RDFXML);
         logger.info("enhance - received results from stanbol server");

         // Parse enhancedText into an rdf document
         List<Statement> rdfDocument = jenaService.parseRdf(enhancedText, "");
         if (rdfDocument != null) {
            // Generate dictionary with properties we are interested in
            properties = generateSolrDocument(rdfDocument, content, docType, title);
            if (title != null && !title.isEmpty()) {
               properties.setProperty("title", title);
            }

            // Add enhanced document to Solr
            logger.info("enhance - about to add document to solr");
            urn = properties.getProperty("about");
            solrClient.addDocument(configuration.getSolrCollection(), properties);
            logger.info("enhance - document added to solr");

            // Store full enhanced doc (rdf) in Jena
            logger.info("enhance - about to store RDF in Jena");
            jenaService.storeRdf(urn, enhancedText, "");
            logger.info("enhance - RDF stored in Jena");

            // Store full document in DB
            logger.info("enhance - saving document in the database");
            Document document = new Document();
            document.setUrn(urn);
            document.setTitle(title);
            document.setType(DocumentType.valueOf(docType));
            document.setContent(content.getBytes());
            documentService.create(document);
            logger.info("enhance - document saved in the database");
         }

      } catch (Exception e) {
         logger.error("enhance - Exception: ", e);
         // if there was any error in the process we remove the documents stored under the URN in process
         if (urn != null) {
            rollbackEnhancement(urn);
         }
         throw e;
      }
   }

   @Override
   public void reindexDocument(String urn, boolean enhance) throws Exception {

      String enhancedText = null;
      Properties properties = null;
      List<Statement> rdfDocument = null;
      Document document = documentService.findByUrn(urn);

      if (document == null) {
         throw new RuntimeException("No document was found under " + urn);
      }

      // Post to Stanbol for enhancement again
      if (enhance) {
         enhancedText = stanbolClient.enhance(configuration.getStanbolChain(), new String(document.getContent()),
               StanbolMediaTypes.RDFXML);
         rdfDocument = jenaService.parseRdf(enhancedText, "");


      // Use cached (already enhanced) version of the document from Jena
      } else {
         rdfDocument = jenaService.readRdf(urn);
      }

      if (rdfDocument != null) {
         // Generate dictionary with properties we are interested in
         properties = generateSolrDocument(rdfDocument, new String(document.getContent()), document.getType().name(), document.getTitle());
         if (document.getTitle() != null && !document.getTitle().isEmpty()) {
            properties.setProperty("title", document.getTitle());
         }

         // Reindex enhanced document in Solr
         solrClient.addDocument(configuration.getSolrCollection(), properties);
      }
   }

   private void rollbackEnhancement(String urn) throws Exception {
      // Delete document from database
      documentService.deleteByUrn(urn);

      // Delete document from Jena
      jenaService.deleteRdf(urn);

      // Delete document from Solr
      solrClient.deleteDocument(configuration.getSolrCollection(), urn);
   }

}
