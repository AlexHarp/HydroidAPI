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
import java.util.*;

/**
 * Created by u24529 on 3/02/2016.
 */
@Service
public class EnhancerServiceImpl implements EnhancerService {

   private static final Logger logger = LoggerFactory.getLogger(EnhancerServiceImpl.class);

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

   @Autowired @Qualifier("googleVisionImageService")
   private ImageService imageService;

   private Properties generateSolrDocument(List<Statement> rdfDocument, String content, String docType, String title) {
      List<String> concepts = new ArrayList<>();
      List<String> labels = new ArrayList<>();
      Properties properties = new Properties();
      StringBuilder documentUrl = new StringBuilder();
      Map<String,String> gaVocabSubjects = new HashMap();
      for (Statement statement : rdfDocument) {

         String subject = statement.getSubject().getLocalName().toLowerCase();
         String predicate = statement.getPredicate().getLocalName().toLowerCase();
         String objectValue = statement.getObject().isLiteral() ? statement.getObject().asLiteral().getString()
               : statement.getObject().asResource().getURI();

         // Discard any statement where the subject is not related to the GAPublicVocabs
         if (configuration.isStoreGAVocabsOnly() && (gaVocabSubjects.get(subject) == null) && !objectValue.contains(GA_PUBLIC_VOCABS)) {
            continue;
         }

         // Check if the predicate is included in the list we are after
         if (ArrayUtils.indexOf(VALID_PREDICATES, predicate) >= 0) {

            if (predicate.equalsIgnoreCase("extracted-from") && properties.getProperty("about") == null) {
               properties.put("about", objectValue);
            } else if (predicate.equalsIgnoreCase("entity-reference")) {

               // Add a new subject to the GA Vocab Subjects list
               if (objectValue.contains(GA_PUBLIC_VOCABS) && gaVocabSubjects.get(subject) == null) {
                  gaVocabSubjects.put(subject, objectValue);
               }

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

      documentUrl.setLength(0);
      documentUrl = new StringBuilder(configuration.getS3OutputUrl()).append("/").append(docType.toLowerCase()).append("s/").append(properties.getProperty("about"));

      // Add property:type to rdf (DOCUMENT, DATASET, MODEL or IMAGE)
      Resource subject = ResourceFactory.createResource(properties.getProperty("about"));
      Property property = ResourceFactory.createProperty("https://www.w3.org/TR/rdf-schema/#ch_type");
      RDFNode object = ResourceFactory.createPlainLiteral(docType);
      Statement statement = ResourceFactory.createStatement(subject, property, object);
      rdfDocument.add(statement);

      // Add property:label to the rdf (the document title)
      property = ResourceFactory.createProperty("https://www.w3.org/TR/rdf-schema/#ch_label");
      object = ResourceFactory.createPlainLiteral(title);
      statement = ResourceFactory.createStatement(subject, property, object);
      rdfDocument.add(statement);

      // Added property:image to the RDF document
      if (docType.equals(DocumentType.DOCUMENT.name())) {
         property = ResourceFactory.createProperty("http://purl.org/dc/dcmitype/Image");
         object = ResourceFactory.createProperty(documentUrl.toString());
         statement = ResourceFactory.createStatement(subject, property, object);
         rdfDocument.add(statement);
      }

      String solrContent = content;
      if (solrContent.length() > 500) {
         solrContent = solrContent.substring(0, 500) + "...";
      }
      properties.put("content", solrContent);
      properties.put("title", title);
      properties.put("label", labels);
      properties.put("concept", concepts);
      properties.put("docType", docType);
      properties.put("docUrl", documentUrl.toString());
      properties.put("creator", DOCUMENT_CREATOR);

      // No labels or concepts were found so we discard
      // the process by clearing all the properties
      if (labels.isEmpty() && concepts.isEmpty()) {
         properties.clear();
      }

      return properties;
   }

   private String getFileNameFromS3ObjectSummary(S3ObjectSummary objectSummary) {
      return objectSummary.getKey().substring(objectSummary.getKey().lastIndexOf("/") + 1);
   }

   @Override
   public boolean enhance(String title, String content, String docType, String origin) {

      String urn = null;
      Properties properties = null;
      boolean enhancementSucceed = false;

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

            // Content has been tagged with our vocabularies
            if (!properties.isEmpty()) {

               // Add enhanced document to Solr
               logger.info("enhance - about to add document to solr");
               urn = properties.getProperty("about");
               solrClient.addDocument(configuration.getSolrCollection(), properties);
               logger.info("enhance - document added to solr");

               // Store full enhanced doc (rdf) in S3
               s3Client.storeFile(configuration.getS3OutputBucket(), configuration.getS3EnhancerOutput() + urn,
                     enhancedText, ContentType.APPLICATION_XML.getMimeType());

               // Also store original image in S3
               if (docType.equals(DocumentType.IMAGE.name())) {
                  logger.info("enhance - saving image in S3 and its metadata in the database");
                  s3Client.copyObject(configuration.getS3Bucket(), configuration.getS3EnhancerInput() + "images/" + title,
                        configuration.getS3OutputBucket(), configuration.getS3EnhancerOutputImages() + urn);
                  saveOrUpdateImageMetadata(origin, content);
                  logger.info("enhance - original image content and metadata saved");
               }

               // Store full document in DB
               logger.info("enhance - saving document in the database");
               saveOrUpdateDocument(origin, urn, title, docType, EnhancementStatus.SUCCESS, null);
               logger.info("enhance - document saved in the database");

               // Store full enhanced doc (rdf) in Jena
               logger.info("enhance - about to store RDF in Jena");
               jenaService.storeRdfDefault(enhancedText, "");
               logger.info("enhance - RDF stored in Jena");

               enhancementSucceed = true;

            } else {
               logger.info("enhance - saving document in the database");
               saveOrUpdateDocument(origin, urn, title, docType, EnhancementStatus.FAILURE,
                     "No matches were found in the vocabularies used by the chain: " + configuration.getStanbolChain());
               logger.info("enhance - document saved in the database");

               // Also store original image metadata
               if (docType.equals(DocumentType.IMAGE.name())) {
                  logger.info("enhance - saving image metadata in the database");
                  saveOrUpdateImageMetadata(origin, content);
                  logger.info("enhance - image metadata saved");
               }
            }
         }

      } catch (Throwable e) {
         logger.error("enhance - Exception: ", e);
         saveOrUpdateDocument(origin, urn, title, docType, EnhancementStatus.FAILURE, e.getLocalizedMessage());

         // Also store original image metadata
         if (docType.equals(DocumentType.IMAGE.name())) {
            logger.info("enhance - saving image metadata in the database");
            saveOrUpdateImageMetadata(origin, content);
            logger.info("enhance - image metadata saved");
         }

         // if there was any error in the process we remove the documents stored under the URN if created
         if (urn != null) {
            rollbackEnhancement(urn);
         }
         throw new HydroidException(e);
      }

      return enhancementSucceed;
   }

   private void saveOrUpdateDocument(String origin, String urn, String title, String docType,
                                     EnhancementStatus status, String statusReason) {
      Document document = documentService.findByOrigin(origin);
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

   private void saveOrUpdateImageMetadata(String origin, String metadata) {
      if (documentService.readImageMetadata(origin) == null) {
         documentService.createImageMetadata(origin, metadata);
      } else {
         documentService.updateImageMetadata(origin, metadata);
      }
   }

   private List<S3ObjectSummary> getDocumentsForEnhancement(List<S3ObjectSummary> input) {
      List<S3ObjectSummary> output = new ArrayList();
      if (!input.isEmpty()) {
         String origin;
         Document document;
         for (S3ObjectSummary object : input) {
            // Ignore folders
            if (!object.getKey().endsWith("/")) {
               origin = object.getBucketName() + ":" + object.getKey();
               document = documentService.findByOrigin(origin);
               // Document was not enhanced or previous enhancement failed
               if (document == null || (document.getStatus() == EnhancementStatus.FAILURE)) {
                  output.add(object);
               }
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
      logger.info("enhanceCollection - there are " + objects.size() + " " + documentType.name().toLowerCase() + "s to be enhanced");
      for (S3ObjectSummary object : objects) {
         s3FileContent = s3Client.getFile(object.getBucketName(), object.getKey());
         fileContent = IOUtils.parseFile(s3FileContent);
         title = getFileNameFromS3ObjectSummary(object);
         origin = configuration.getS3Bucket() + ":" + object.getKey();
         try {
            enhance(title, fileContent, documentType.name(), origin);
         } catch (Throwable e) {
            logger.error("enhanceCollection - error processing file key: " + object.getKey());
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
      InputStream s3FileContent;
      String key = configuration.getS3EnhancerInput() + DocumentType.IMAGE.name().toLowerCase() + "s";
      List<S3ObjectSummary> objects = s3Client.listObjects(configuration.getS3Bucket(), key);
      List<S3ObjectSummary> objectsForEnhancement = getDocumentsForEnhancement(objects);
      logger.info("enhanceImages - there are " + objectsForEnhancement.size() + " images to be enhanced");
      logger.info("enhanceImages - " + (objects.size() - objectsForEnhancement.size()) + " images will be taken from the cache");
      String imageMetadata;
      for (S3ObjectSummary object : objects) {
         // Ignore folders
         if (!object.getKey().endsWith("/")) {
            title = getFileNameFromS3ObjectSummary(object);
            origin = configuration.getS3Bucket() + ":" + object.getKey();
            // The image metadata will be extracted and used for enhancement
            if (objectsForEnhancement.contains(object)) {
               s3FileContent = s3Client.getFile(object.getBucketName(), object.getKey());
               imageMetadata = title + "\n" + imageService.getImageMetadata(s3FileContent);
               // The cached imaged metadata will be used for enhancement
            } else {
               imageMetadata = documentService.readImageMetadata(origin);
            }
            try {
               enhance(title, imageMetadata, DocumentType.IMAGE.name(), origin);
            } catch (Throwable e) {
               logger.error("enhanceImages - error processing file key: " + object.getKey());
            }
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
