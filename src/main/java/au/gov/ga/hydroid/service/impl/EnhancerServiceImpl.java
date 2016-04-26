package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.DocumentDTO;
import au.gov.ga.hydroid.dto.ImageAnnotation;
import au.gov.ga.hydroid.dto.ImageMetadata;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.model.EnhancementStatus;
import au.gov.ga.hydroid.service.*;
import au.gov.ga.hydroid.utils.HydroidException;
import au.gov.ga.hydroid.utils.IOUtils;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.entity.ContentType;
import org.apache.jena.rdf.model.*;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AbstractParser;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Created by u24529 on 3/02/2016.
 */
@Service
public class EnhancerServiceImpl implements EnhancerService {

   private static final Logger logger = LoggerFactory.getLogger(EnhancerServiceImpl.class);

   private static final List<String> VALID_PREDICATES = Arrays.asList("extracted-from", "entity-reference", "entity-label", "selection-context");
   private static final String GA_PUBLIC_VOCABS = "GAPublicVocabsSandbox";

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   private StanbolClient stanbolClient;

   @Autowired
   private SolrClient solrClient;

   @Autowired
   private JenaService jenaService;

   @Autowired
   @Value("#{systemProperties['s3.use.file.system'] != null ? s3FileSystem : s3ClientImpl}")
   private S3Client s3Client;

   @Autowired
   private DocumentService documentService;

   @Autowired
   @Value("#{systemProperties['use.local.image.service'] != null ? localImageService : googleVisionImageService}")
   private ImageService imageService;

   @Autowired
   private ApplicationContext applicationContext;

   // Add a new subject to the GA Vocab Subjects list
   private void addGAVocabSubject(Map<String,String> gaVocabSubjects, String subject, String objectValue) {
      if (objectValue.contains(GA_PUBLIC_VOCABS) && gaVocabSubjects.get(subject) == null) {
         gaVocabSubjects.put(subject, objectValue);
      }
   }

   // add new value if not there yet
   private void addMultiValuedField(List<String> multiValuedField, String objectValue) {
      if (!multiValuedField.contains(objectValue)) {
         multiValuedField.add(objectValue);
      }
   }

   private Properties initProperties(DocumentDTO document) {
      Properties properties = new Properties();
      properties.put("content", document.getContent());
      properties.put("title", document.getTitle());
      properties.put("docType", document.getDocType());
      if (document.getAuthor() != null) {
         properties.put("creator", document.getAuthor());
      }
      if (document.getDateCreated() != null) {
         properties.put("created", document.getDateCreated());
      }
      return properties;
   }

   private void addStatementToRDF(List<Statement> rdfDocument, Resource subject, String propertyName, String value) {
      Property property = ResourceFactory.createProperty(propertyName);
      RDFNode object = ResourceFactory.createPlainLiteral(value);
      rdfDocument.add(ResourceFactory.createStatement(subject, property, object));
   }

   private String addStatementsToRDF(List<Statement> rdfDocument, String about, DocumentDTO document) {
      String documentUrl = configuration.getS3OutputUrl()
            + (document.getDocType().equals(DocumentType.IMAGE.name()) ? "/images/" : "/rdfs/")
            + about;

      Resource subject = ResourceFactory.createResource(about);

      // Add property:type to rdf (DOCUMENT, DATASET, MODEL or IMAGE)
      addStatementToRDF(rdfDocument, subject, "https://www.w3.org/TR/rdf-schema/#ch_type", document.getDocType());

      // Add property:label to the rdf (the document title)
      addStatementToRDF(rdfDocument, subject, "https://www.w3.org/TR/rdf-schema/#ch_label", document.getTitle());

      // Added property:image to the RDF document
      if (document.getDocType().equals(DocumentType.IMAGE.name())) {
         addStatementToRDF(rdfDocument, subject, "http://purl.org/dc/dcmitype/Image", documentUrl);
      }

      return documentUrl;
   }

   private Properties generateSolrDocument(List<Statement> rdfDocument, DocumentDTO document) {
      List<String> concepts = new ArrayList<>();
      List<String> labels = new ArrayList<>();
      List<String> selectionContexts = new ArrayList<>();
      Map<String,String> gaVocabSubjects = new HashMap<>();
      Properties properties = initProperties(document);

      for (Statement statement : rdfDocument) {
         String subject = statement.getSubject().getLocalName().toLowerCase();
         String predicate = statement.getPredicate().getLocalName().toLowerCase();
         String objectValue = statement.getObject().isLiteral() ? statement.getObject().asLiteral().getString()
               : statement.getObject().asResource().getURI();

         // Discard if the predicate is not included in the list we are after
         if (!VALID_PREDICATES.contains(predicate)) {
            continue;
         }

         if ("extracted-from".equalsIgnoreCase(predicate) && !properties.containsKey("about")) {
            properties.put("about", objectValue);

         } else if ("entity-reference".equalsIgnoreCase(predicate)) {
            addGAVocabSubject(gaVocabSubjects, subject, objectValue);
            addMultiValuedField(concepts, objectValue);

         } else if ("entity-label".equalsIgnoreCase(predicate)) {
            addMultiValuedField(labels, objectValue);

         } else if ("selection-context".equalsIgnoreCase(predicate)) {
            addMultiValuedField(selectionContexts, objectValue);

         } else {
            properties.put(predicate, objectValue);
         }
      }

      // GAPublicVocabs is required but none was found
      boolean isGAVocabsNotValid = configuration.isStoreGAVocabsOnly() && gaVocabSubjects.isEmpty();

      // No labels or concepts were found so we discard the process by clearing all the properties
      if (isGAVocabsNotValid || (labels.isEmpty() && concepts.isEmpty())) {
         properties.clear();
         return properties;
      }

      // Add additional statements to RDF and generate documentURL
      String documentUrl = addStatementsToRDF(rdfDocument, properties.getProperty("about"), document);

      properties.put("label", labels);
      properties.put("concept", concepts);
      properties.put("docUrl", documentUrl);
      properties.put("selectionContext", selectionContexts);

      return properties;
   }

   private String getFileNameFromS3ObjectSummary(DataObjectSummary objectSummary) {
      return objectSummary.getKey().substring(objectSummary.getKey().lastIndexOf("/") + 1);
   }

   private String getImageThumb(BufferedImage image, String title, String urn) {
      try {
         BufferedImage resized = Scalr.resize(image, 200);
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         ImageIO.write(resized,"png", os);
         InputStream byteArrayInputStream = new ByteArrayInputStream(os.toByteArray());
         s3Client.storeFile(
               configuration.getS3OutputBucket(),
               configuration.getS3EnhancerOutputImages() + urn + "_thumb",
               byteArrayInputStream,
               "image/png");
         return configuration.getS3OutputUrl() + "/images/" + urn + "_thumb";
      } catch (Exception e) {
         logger.error("Failed to resize image '" + title + "'.", e);
         return null;
      }
   }

   @Override
   public boolean enhance(DocumentDTO document) {

      String urn = null;

      try {

         // Send content to Stanbol for enhancement
         logger.info("enhance - about to post to stanbol server");
         String enhancedText = stanbolClient.enhance(configuration.getStanbolChain(), document.getContent(), StanbolMediaTypes.RDFXML);
         logger.info("enhance - received results from stanbol server");
         enhancedText = StringUtils.replace(enhancedText, ":content-item-sha1-", ":content-item-sha1:");
         logger.info("enhance - changed urn pattern, still contain old: " + enhancedText.contains(":content-item-sha1-"));

         // Parse enhancedText into an rdf document
         List<Statement> rdfDocument = jenaService.parseRdf(enhancedText, "");
         if (rdfDocument == null) {
            return false;
         }

         // Generate dictionary with properties we are interested in
         Properties properties = generateSolrDocument(rdfDocument, document);
         urn = properties.getProperty("about");

         // Content has NOT been tagged with our vocabularies
         if (properties.isEmpty()) {
            logger.info("enhance - saving document in the database");
            saveOrUpdateDocument(document, urn, EnhancementStatus.FAILURE,
                  "No matches were found in the vocabularies used by the chain: " + configuration.getStanbolChain());
            logger.info("enhance - document saved in the database");

            // Also store original image metadata
            if (document.getDocType().equals(DocumentType.IMAGE.name())) {
               logger.info("enhance - saving image metadata in the database");
               saveOrUpdateImageMetadata(document.getOrigin(), document.getContent());
               logger.info("enhance - image metadata saved");
            }

            return false;
         }

         // Store full enhanced doc (rdf) in S3
         s3Client.storeFile(configuration.getS3OutputBucket(), configuration.getS3EnhancerOutput() + urn,
               enhancedText, ContentType.APPLICATION_XML.getMimeType());

         // Also store original image in S3
         if (document.getDocType().equals(DocumentType.IMAGE.name())) {
            logger.info("enhance - saving image in S3 and its metadata in the database");
            s3Client.copyObject(configuration.getS3Bucket(), configuration.getS3EnhancerInput() + "images/" + document.getTitle(),
                    configuration.getS3OutputBucket(), configuration.getS3EnhancerOutputImages() + urn);
            saveOrUpdateImageMetadata(document.getOrigin(), document.getContent());
            logger.info("enhance - original image content and metadata saved");
            InputStream origImage = s3Client.getFile(configuration.getS3Bucket(), configuration.getS3EnhancerInput() + "images/" + document.getTitle());
            BufferedImage image = ImageIO.read(origImage);
            properties.put("imgThumb", getImageThumb(image, document.getTitle(), urn));
         }

         // Add enhanced document to Solr
         logger.info("enhance - about to add document to solr");
         solrClient.addDocument(configuration.getSolrCollection(), properties);
         logger.info("enhance - document added to solr");

         // Store full document in DB
         logger.info("enhance - saving document in the database");
         saveOrUpdateDocument(document, urn, EnhancementStatus.SUCCESS, null);
         logger.info("enhance - document saved in the database");

         // Store full enhanced doc (rdf) in Jena
         logger.info("enhance - about to store RDF in Jena");
         jenaService.storeRdfDefault(enhancedText, "");
         logger.info("enhance - RDF stored in Jena");

         return true;

      } catch (Exception e) {
         logger.error("enhance - Exception: ", e);
         saveOrUpdateDocument(document, urn, EnhancementStatus.FAILURE, e.getLocalizedMessage());

         // Also store original image metadata
         if (document.getDocType().equals(DocumentType.IMAGE.name())) {
            logger.info("enhance - saving image metadata in the database");
            saveOrUpdateImageMetadata(document.getOrigin(), document.getContent());
            logger.info("enhance - image metadata saved");
         }

         // if there was any error in the process we remove the documents stored under the URN if created
         rollbackEnhancement(urn);

         throw new HydroidException(e);
      }
   }

   private void saveOrUpdateDocument(DocumentDTO documentDTO, String urn, EnhancementStatus status, String statusReason) {
      Document document = documentService.findByOrigin(documentDTO.getOrigin());
      if (document == null) {
         document = new Document();
      }
      document.setOrigin(documentDTO.getOrigin());
      document.setUrn(urn);
      document.setTitle(documentDTO.getTitle());
      document.setType(DocumentType.valueOf(documentDTO.getDocType()));
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

   private List<DataObjectSummary> getDocumentsForEnhancement(List<DataObjectSummary> input) {
      List<DataObjectSummary> output = new ArrayList<>();
      if (input.isEmpty()) {
         return output;
      }
      String origin;
      Document document;
      for (DataObjectSummary object : input) {
         // Ignore folders
         if (object.getKey().endsWith("/")) {
            continue;
         }
         origin = object.getBucketName() + ":" + object.getKey();
         document = documentService.findByOrigin(origin);
         // Document was not enhanced or previous enhancement failed
         if (document == null || (document.getStatus() == EnhancementStatus.FAILURE)) {
            output.add(object);
         }
      }
      return output;
   }

   private void copyMetadataToDocument(Metadata metadata, DocumentDTO document, String fallBackTitle) {
      if (metadata.get("title") != null) {
         document.setTitle(metadata.get("title"));
      } else if (metadata.get("dc:title") != null) {
         document.setTitle(metadata.get("dc:title"));
      } else {
         document.setTitle(fallBackTitle);
      }
      document.setAuthor(metadata.get("author") == null ? metadata.get("Author") : metadata.get("author"));
      document.setDateCreated(metadata.get("Creation-Date") == null ? null :
            DateUtils.parseDate(metadata.get("Creation-Date"), new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"}));
   }

   private void enhanceCollection(DocumentType documentType) {
      Metadata metadata;
      DocumentDTO document;
      InputStream s3FileContent;
      String key = configuration.getS3EnhancerInput() + documentType.name().toLowerCase() + "s";
      List<DataObjectSummary> objects = s3Client.listObjects(configuration.getS3Bucket(), key);
      objects = getDocumentsForEnhancement(objects);
      logger.info("enhanceCollection - there are " + objects.size() + " " + documentType.name().toLowerCase() + "s to be enhanced");
      for (DataObjectSummary object : objects) {

         s3FileContent = s3Client.getFile(object.getBucketName(), object.getKey());

         metadata = new Metadata();
         document = new DocumentDTO();
         document.setContent(IOUtils.parseStream(s3FileContent, metadata));
         document.setOrigin(configuration.getS3Bucket() + ":" + object.getKey());
         copyMetadataToDocument(metadata, document, getFileNameFromS3ObjectSummary(object));

         try {
            enhance(document);
         } catch (Exception e) {
            logger.error("enhanceCollection - error processing file key: " + object.getKey(), e);
         }
      }
   }

   private String getImageMetadataAsString(InputStream s3FileContent) {
      StringBuilder result = new StringBuilder();
      ImageMetadata imageMetadata = imageService.getImageMetadata(s3FileContent);
      for (ImageAnnotation imageLabel : imageMetadata.getImageLabels()) {
         result.append(imageLabel.getDescription()).append(" (").append(imageLabel.getScore()).append("),");
      }
      result.setLength(result.length() - 1);
      return result.toString();
   }

   private void enhancePendingDocuments() {
      Metadata metadata;
      DocumentDTO document;

      List<Document> documents = documentService.findByStatus(EnhancementStatus.PENDING);
      logger.info("enhancePendingDocuments - there are " + documents.size() + " pending documents to be enhanced");
      for (Document dbDocument : documents) {

         metadata = new Metadata();
         document = new DocumentDTO();
         InputStream inputStream = IOUtils.getUrlContent(dbDocument.getOrigin());

         // User custom parser
         if (dbDocument.getParserName() != null) {
            AbstractParser parser = (AbstractParser) applicationContext.getBean(dbDocument.getParserName());
            document.setContent(IOUtils.parseStream(inputStream, metadata, parser));
         // Use default parser
         } else {
            document.setContent(IOUtils.parseStream(inputStream, metadata));
         }

         document.setOrigin(dbDocument.getOrigin());
         copyMetadataToDocument(metadata, document, dbDocument.getOrigin());

         try {
            enhance(document);
         } catch (Exception e) {
            logger.error("enhancePendingDocuments - error processing URL: " + dbDocument.getOrigin(), e);
         }
      }
   }

   @Override
   public void enhanceDocuments() {
      enhanceCollection(DocumentType.DOCUMENT);
      enhancePendingDocuments();
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
      DocumentDTO document;
      InputStream s3FileContent;
      String key = configuration.getS3EnhancerInput() + DocumentType.IMAGE.name().toLowerCase() + "s";
      List<DataObjectSummary> objectsForEnhancement = getDocumentsForEnhancement(s3Client.listObjects(configuration.getS3Bucket(), key));
      logger.info("enhanceImages - there are " + objectsForEnhancement.size() + " images to be enhanced");
      for (DataObjectSummary s3ObjectSummary : objectsForEnhancement) {

         // Ignore folders
         if (s3ObjectSummary.getKey().endsWith("/")) {
            continue;
         }

         document = new DocumentDTO();
         document.setDocType(DocumentType.IMAGE.name());
         document.setTitle(getFileNameFromS3ObjectSummary(s3ObjectSummary));
         document.setOrigin(configuration.getS3Bucket() + ":" + s3ObjectSummary.getKey());

         // The cached imaged metadata will be used for enhancement (if exists)
         document.setContent(documentService.readImageMetadata(document.getOrigin()));

         // The image metadata will be extracted and used for enhancement
         if (document.getContent() == null) {
            s3FileContent = s3Client.getFile(s3ObjectSummary.getBucketName(), s3ObjectSummary.getKey());
            document.setContent(document.getTitle() + "\n" + getImageMetadataAsString(s3FileContent));
         }

         try {
            enhance(document);
         } catch (Exception e) {
            logger.error("enhanceImages - error processing file key: " + s3ObjectSummary.getKey(), e);
         }
      }
   }

   private void rollbackEnhancement(String urn) {
      if (urn == null) {
         return;
      }

      // Delete document from S3
      s3Client.deleteFile(configuration.getS3Bucket(), configuration.getS3EnhancerOutput() + urn);

      // Delete document from Solr
      solrClient.deleteDocument(configuration.getSolrCollection(), urn);
   }

}
