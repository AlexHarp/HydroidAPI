package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.service.*;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.entity.ContentType;
import org.openrdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

/**
 * Created by u24529 on 3/02/2016.
 */
@Service
public class EnhancerServiceImpl implements EnhancerService {

   private static final String[] VALID_PREDICATES = {"title", "subject", "created", "extracted-from", "entity-reference", "entity-label"};
   private static final String GA_PUBLIC_VOCABS = "GAPublicVocabsSandbox";

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   private StanbolClient stanbolClient;

   @Autowired
   private SolrClient solrClient;

   @Autowired
   private S3Client s3Client;

   @Autowired
   private DocumentService documentService;

   private Properties generateSolrDocument(List<Statement> rdfDocument, String content) {
      String predicate = null;
      String currentSubject = null;
      boolean isValidEntityRef = true;
      StringBuilder concept = new StringBuilder();
      StringBuilder label = new StringBuilder();
      Properties properties = new Properties();
      for (Statement statement : rdfDocument) {
         predicate = statement.getPredicate().getLocalName().toLowerCase();
         if (!statement.getSubject().stringValue().equalsIgnoreCase(currentSubject)) {
            currentSubject = statement.getSubject().stringValue();
            //isValidEntityRef = false;
         }
         if (ArrayUtils.indexOf(VALID_PREDICATES, predicate) >= 0) {
            if (predicate.equalsIgnoreCase("extracted-from") && properties.getProperty("about") == null) {
               properties.put("about", statement.getObject().stringValue());
            } else if (predicate.equalsIgnoreCase("entity-reference")) {
               isValidEntityRef = (statement.getObject().stringValue().indexOf(GA_PUBLIC_VOCABS) >= 0);
               // add new concept if not there yet
               if (isValidEntityRef && concept.indexOf(predicate) < 0) {
                  concept.append(statement.getObject().stringValue()).append(", ");
               }
            } else if (predicate.equalsIgnoreCase("entity-label")) {
               // add new label if not there yet
               if (isValidEntityRef && label.indexOf(predicate) < 0) {
                  label.append(statement.getObject().stringValue()).append(", ");
               }
            } else {
               properties.put(predicate, statement.getObject().stringValue());
            }
         }
      }

      String solrContent = content;
      if (solrContent.length() > 500) {
         solrContent = solrContent.substring(0, 500) + "...";
      }
      properties.put("content", solrContent);
      properties.put("label", label.toString());
      properties.put("concept", concept.toString());
      properties.put("creator", "Hydroid Enhancer App");

      return properties;
   }

   @Override
   public void enhance(String chainName, String title, String content, String solrCollection) throws Exception {

      // Send content to Stanbol for enhancement
      String enhancedText = stanbolClient.enhance(chainName, content, StanbolMediaTypes.RDFXML);

      // Parse enhancedText into an rdf document
      List<Statement> rdfDocument = stanbolClient.parseRDF(enhancedText);
      if (rdfDocument != null) {
         // Generate dictionary with properties we are interested in
         Properties properties = generateSolrDocument(rdfDocument, content);

         // Add enhanced document to Solr
         solrClient.addDocument(solrCollection, properties);

         // Store full enhanced doc (rdf) at S3
         s3Client.storeFile(configuration.getS3Bucket(), configuration.getS3RDFFolder() + properties.getProperty("about"), enhancedText,
               ContentType.APPLICATION_XML.getMimeType());

         // Store full document in DB
         Document document = new Document();
         document.setUrn(properties.getProperty("about"));
         document.setTitle(title);
         document.setContent(content.getBytes());
         documentService.create(document);

         //todo if database operation failed, need to remove doc from Solr
      } else {
         // todo error handling here as well
      }
   }

}
