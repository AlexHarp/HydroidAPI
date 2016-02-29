package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.model.EnhancementStatus;
import au.gov.ga.hydroid.service.*;
import au.gov.ga.hydroid.utils.HydroidException;
import au.gov.ga.hydroid.utils.IOUtils;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.InputStream;
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
   private S3Client s3Client;

   @Autowired
   private DocumentService documentService;

   @Autowired @Qualifier("imageServiceImpl")
   private ImageService imageService;

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

   private String getFileNameFromS3ObjectSummary(S3ObjectSummary objectSummary) {
      return objectSummary.getKey().substring(objectSummary.getKey().lastIndexOf("/") + 1);
   }

   @Override
   public void enhance(String title, String content, String docType, String origin) {

      String urn = null;
      Properties properties = null;

      try {

         // Send content to Stanbol for enhancement
         logger.info("enhance - about to post to stanbol server");
         String enhancedText = stanbolClient.enhance(configuration.getStanbolChain(), content, StanbolMediaTypes.RDFXML);
         logger.info("enhance - received results from stanbol server");
         enhancedText = StringUtils.replace(enhancedText, ":content-item-sha1-", ":content-item-sha1:");
         logger.info("enhance - changed urn pattern, still contain old: " + enhancedText.contains(":content-item-sha1-"));

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

            // Store full enhanced doc (rdf) in S3
            s3Client.storeFile(configuration.getS3Bucket(), configuration.getS3EnhancerOutput() + urn, content, ContentType.APPLICATION_XML.getMimeType());

            // Store full document in DB
            logger.info("enhance - saving document in the database");
            saveOrUpdateDocument(origin, urn, title, docType, EnhancementStatus.SUCCESS, null);
            logger.info("enhance - document saved in the database");

            // Store full enhanced doc (rdf) in Jena
            logger.info("enhance - about to store RDF in Jena");
            jenaService.storeRdfDefault(enhancedText, "");
            logger.info("enhance - RDF stored in Jena");
         }

      } catch (Throwable e) {
         logger.error("enhance - Exception: ", e);
         // if there was any error in the process we remove the documents stored under the URN in process
         if (urn != null) {
            saveOrUpdateDocument(origin, urn, title, docType, EnhancementStatus.FAILURE, e.getLocalizedMessage());
            rollbackEnhancement(urn);
         }
         throw new HydroidException(e);
      }
   }

   private void saveOrUpdateDocument(String origin, String urn, String title, String docType,
                                     EnhancementStatus status, String statusReason) {
      Document document = documentService.findByUrn(urn);
      if (document == null) {
         document = new Document();
      }
      document.setOrigin(origin);
      document.setUrn(urn);
      document.setTitle(title);
      document.setType(DocumentType.valueOf(docType));
      document.setStatus(status);
      document.setStatusReason(statusReason);
      if (document.getId() == 0) {
         documentService.create(document);
      } else {
         documentService.update(document);
      }
   }

   @Override
   public void reindexDocument(String urn, boolean enhance) {

      String enhancedText = null;
      Properties properties = null;
      List<Statement> rdfDocument = null;
      Document document = documentService.findByUrn(urn);

      if (document == null) {
         throw new RuntimeException("No document was found under " + urn);
      }

      // Post to Stanbol for enhancement again
      if (enhance) {
         //enhancedText = stanbolClient.enhance(configuration.getStanbolChain(), new String(document.getContent()),
               //StanbolMediaTypes.RDFXML);

         // Use cached (already enhanced) version of the document from S3
      } else {
         enhancedText = new String(s3Client.getFileAsByteArray(configuration.getS3Bucket(), configuration.getS3EnhancerOutput() + urn));
      }

      // Parse the enhanced content String into an rdfDocument
      rdfDocument = jenaService.parseRdf(enhancedText, "");

      // Generate dictionary with properties we are interested in
      //properties = generateSolrDocument(rdfDocument, new String(document.getContent()), document.getType().name(), document.getTitle());
      if (document.getTitle() != null && !document.getTitle().isEmpty()) {
         properties.setProperty("title", document.getTitle());
      }

      // Save the new enhanced document in Solr
      solrClient.addDocument(configuration.getSolrCollection(), properties);

      // If new enhancement was run we save the RDF in S3 and Jena
      if (enhance) {
         s3Client.storeFile(configuration.getS3Bucket(), configuration.getS3EnhancerOutput() + urn, enhancedText, ContentType.APPLICATION_XML.getMimeType());
         jenaService.storeRdfDefault(enhancedText, "");
      }
   }

   private List<S3ObjectSummary> getDocumentsForEnhancement(List<S3ObjectSummary> input) {
      List<S3ObjectSummary> output = new ArrayList();
      if (!input.isEmpty()) {
         String origin;
         Document document;
         // remove the folder item
         input.remove(0);
         for (S3ObjectSummary object : input) {
            origin = object.getBucketName() + ":" + object.getKey();
            document = documentService.findByOrigin(origin);
            // Document was not enhanced or previous enhancement failed
            if (document == null || document.getStatus() == EnhancementStatus.FAILURE) {
               output.add(object);
            }
         }
      }
      return output;
   }

   private void enhanceCollection(DocumentType documentType) {
      String title;
      String origin;
      String fileContent;
      InputStream s3FileContent;
      String key = configuration.getS3EnhancerInput() + documentType.name().toLowerCase() + "s";
      List<S3ObjectSummary> objects = s3Client.listObjects(configuration.getS3Bucket(), key);
      objects = getDocumentsForEnhancement(objects);
      for (S3ObjectSummary object : objects) {
         s3FileContent = s3Client.getFile(object.getBucketName(), object.getKey());
         fileContent = IOUtils.parseFile(s3FileContent);
         title = getFileNameFromS3ObjectSummary(object);
         origin = configuration.getS3Bucket() + ":" + object.getKey();
         try {
            enhance(title, fileContent, documentType.name(), origin);
         } catch (Throwable e) {
            logger.error("enhanceCollection - error processing file key: " + key);
         }
      }
   }

   @Override
   public void enhanceDocuments() {
      enhanceCollection(DocumentType.DOCUMENT);
   }

   @Override
   public void enhanceDatasets() {
      enhanceCollection(DocumentType.DATASET);
   }

   @Override
   public void enhanceModels() {
      enhanceCollection(DocumentType.MODEL);
   }

   @Override
   public void enhanceImages() {
      String title;
      String origin;
      String fileContent;
      String metadata;
      InputStream s3FileContent;
      String key = configuration.getS3EnhancerInput() + DocumentType.IMAGE.name().toLowerCase() + "s";
      List<S3ObjectSummary> objects = s3Client.listObjects(configuration.getS3Bucket(), key);
      objects = getDocumentsForEnhancement(objects);
      for (S3ObjectSummary object : objects) {
         s3FileContent = s3Client.getFile(object.getBucketName(), object.getKey());
         String imageMetadata = imageService.getImageMetadata(s3FileContent);
         title = getFileNameFromS3ObjectSummary(object);
         // Adding title to imageMetadata to guarantee an unique URN
         imageMetadata = title + "\n" + imageMetadata;
         origin = configuration.getS3Bucket() + ":" + object.getKey();
         try {
            enhance(title, imageMetadata, DocumentType.IMAGE.name(), origin);
         } catch (Throwable e) {
            logger.error("enhanceImages - error processing file key: " + key);
         }
      }
   }

   private void rollbackEnhancement(String urn) {
      // Delete document from S3
      s3Client.deleteFile(configuration.getS3Bucket(), configuration.getS3EnhancerOutput() + urn);

      // Delete document from Solr
      solrClient.deleteDocument(configuration.getSolrCollection(), urn);
   }

}
