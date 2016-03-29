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
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.entity.ContentType;
import org.apache.jena.rdf.model.*;
import org.apache.tika.metadata.Metadata;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

   private static final String[] VALID_PREDICATES = {"extracted-from", "entity-reference", "entity-label", "selection-context"};
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
   private S3Client s3Client;

   @Autowired
   private DocumentService documentService;

   @Autowired @Qualifier("googleVisionImageService")
   private ImageService imageService;

   private Properties generateSolrDocument(List<Statement> rdfDocument, DocumentDTO document) {
      List<String> concepts = new ArrayList<>();
      List<String> labels = new ArrayList<>();
      List<String> selectionContexts = new ArrayList<>();
      Properties properties = new Properties();
      StringBuilder documentUrl = new StringBuilder();
      Map<String,String> gaVocabSubjects = new HashMap<>();
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
            } else if (predicate.equalsIgnoreCase("selection-context")) {
               // add new selection-context if not there yet
               if (!selectionContexts.contains(objectValue)) {
                  selectionContexts.add(objectValue);
               }
            } else {
               properties.put(predicate, objectValue);
            }
         }
      }

      documentUrl.setLength(0);
      documentUrl = new StringBuilder(configuration.getS3OutputUrl())
            .append(document.docType.equals(DocumentType.IMAGE.name()) ? "/images/" : "/rdfs/")
            .append(properties.getProperty("about"));

      // Add property:type to rdf (DOCUMENT, DATASET, MODEL or IMAGE)
      Resource subject = ResourceFactory.createResource(properties.getProperty("about"));
      Property property = ResourceFactory.createProperty("https://www.w3.org/TR/rdf-schema/#ch_type");
      RDFNode object = ResourceFactory.createPlainLiteral(document.docType);
      Statement statement = ResourceFactory.createStatement(subject, property, object);
      rdfDocument.add(statement);

      // Add property:label to the rdf (the document title)
      property = ResourceFactory.createProperty("https://www.w3.org/TR/rdf-schema/#ch_label");
      object = ResourceFactory.createPlainLiteral(document.title);
      statement = ResourceFactory.createStatement(subject, property, object);
      rdfDocument.add(statement);

      // Added property:image to the RDF document
      if (document.docType.equals(DocumentType.IMAGE.name())) {
         property = ResourceFactory.createProperty("http://purl.org/dc/dcmitype/Image");
         object = ResourceFactory.createProperty(documentUrl.toString());
         statement = ResourceFactory.createStatement(subject, property, object);
         rdfDocument.add(statement);
      }

      properties.put("content", document.content);
      properties.put("title", document.title);
      properties.put("label", labels);
      properties.put("concept", concepts);
      properties.put("docType", document.docType);
      properties.put("docUrl", documentUrl.toString());
      if (document.author != null) {
         properties.put("creator", document.author);
      }
      if (document.dateCreated != null) {
         properties.put("created", document.dateCreated);
      }
      properties.put("selectionContext", selectionContexts);

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
   public boolean enhance(DocumentDTO document) {

      String urn = null;
      Properties properties = null;
      boolean enhancementSucceed = false;

      try {

         // Send content to Stanbol for enhancement
         logger.info("enhance - about to post to stanbol server");
         String enhancedText = stanbolClient.enhance(configuration.getStanbolChain(), document.content, StanbolMediaTypes.RDFXML);
         logger.info("enhance - received results from stanbol server");
         enhancedText = StringUtils.replace(enhancedText, ":content-item-sha1-", ":content-item-sha1:");
         logger.info("enhance - changed urn pattern, still contain old: " + enhancedText.contains(":content-item-sha1-"));

         // Parse enhancedText into an rdf document
         List<Statement> rdfDocument = jenaService.parseRdf(enhancedText, "");
         if (rdfDocument != null) {
            // Generate dictionary with properties we are interested in
            properties = generateSolrDocument(rdfDocument, document);
            urn = properties.getProperty("about");
            // Content has been tagged with our vocabularies
            if (!properties.isEmpty()) {

               // Store full enhanced doc (rdf) in S3
               s3Client.storeFile(configuration.getS3OutputBucket(), configuration.getS3EnhancerOutput() + urn,
                     enhancedText, ContentType.APPLICATION_XML.getMimeType());

               // Also store original image in S3
               if (document.docType.equals(DocumentType.IMAGE.name())) {
                  logger.info("enhance - saving image in S3 and its metadata in the database");
                  s3Client.copyObject(configuration.getS3Bucket(), configuration.getS3EnhancerInput() + "images/" + document.title,
                          configuration.getS3OutputBucket(), configuration.getS3EnhancerOutputImages() + urn);
                  saveOrUpdateImageMetadata(document.origin, document.content);
                  logger.info("enhance - original image content and metadata saved");
                  InputStream origImage = s3Client.getFile(configuration.getS3Bucket(), configuration.getS3EnhancerInput() + "images/" + document.title);
                  BufferedImage image = ImageIO.read(origImage);

                  try {
                     BufferedImage resized = Scalr.resize(image,200);
                     ByteArrayOutputStream os = new ByteArrayOutputStream();
                     ImageIO.write(resized,"png", os);
                     InputStream byteArrayInputStream = new ByteArrayInputStream(os.toByteArray());
                     s3Client.storeFile(
                             configuration.getS3OutputBucket(),
                             configuration.getS3EnhancerOutputImages() + urn + "_thumb",
                             byteArrayInputStream,
                             "image/png");
                     properties.put("imgThumb",configuration.getS3OutputUrl() + "/images/" + urn + "_thumb");
                  } catch (Exception e) {
                     logger.error("Failed to resize image '" + document.title + "'.",e);
                  }
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

               enhancementSucceed = true;

            } else {
               logger.info("enhance - saving document in the database");
               saveOrUpdateDocument(document, urn, EnhancementStatus.FAILURE,
                     "No matches were found in the vocabularies used by the chain: " + configuration.getStanbolChain());
               logger.info("enhance - document saved in the database");

               // Also store original image metadata
               if (document.docType.equals(DocumentType.IMAGE.name())) {
                  logger.info("enhance - saving image metadata in the database");
                  saveOrUpdateImageMetadata(document.origin, document.content);
                  logger.info("enhance - image metadata saved");
               }
            }
         }

      } catch (Throwable e) {
         logger.error("enhance - Exception: ", e);
         saveOrUpdateDocument(document, urn, EnhancementStatus.FAILURE, e.getLocalizedMessage());

         // Also store original image metadata
         if (document.docType.equals(DocumentType.IMAGE.name())) {
            logger.info("enhance - saving image metadata in the database");
            saveOrUpdateImageMetadata(document.origin, document.content);
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

   private void saveOrUpdateDocument(DocumentDTO documentDTO, String urn, EnhancementStatus status, String statusReason) {
      Document document = documentService.findByOrigin(documentDTO.origin);
      if (document == null) {
         document = new Document();
      }
      document.setOrigin(documentDTO.origin);
      document.setUrn(urn);
      document.setTitle(documentDTO.title);
      document.setType(DocumentType.valueOf(documentDTO.docType));
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
      Metadata metadata;
      DocumentDTO document;
      InputStream s3FileContent;
      String key = configuration.getS3EnhancerInput() + documentType.name().toLowerCase() + "s";
      List<S3ObjectSummary> objects = s3Client.listObjects(configuration.getS3Bucket(), key);
      objects = getDocumentsForEnhancement(objects);
      logger.info("enhanceCollection - there are " + objects.size() + " " + documentType.name().toLowerCase() + "s to be enhanced");
      for (S3ObjectSummary object : objects) {

         s3FileContent = s3Client.getFile(object.getBucketName(), object.getKey());

         metadata = new Metadata();
         document = new DocumentDTO();
         document.content = IOUtils.parseFile(s3FileContent, metadata);

         // Get document title
         if (metadata.get("title") != null) {
            document.title = metadata.get("title");
         } else if (metadata.get("dc:title") != null) {
            document.title = metadata.get("dc:title");
         } else {
            document.title = getFileNameFromS3ObjectSummary(object);
         }

         document.author = metadata.get("author") == null ? metadata.get("Author") : metadata.get("author");
         document.dateCreated = metadata.get("Creation-Date") == null ? null :
               DateUtils.parseDate(metadata.get("Creation-Date"), new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"});
         document.origin = configuration.getS3Bucket() + ":" + object.getKey();

         try {
            enhance(document);
         } catch (Throwable e) {
            logger.error("enhanceCollection - error processing file key: " + object.getKey());
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
      //String title;
      //String origin;
      DocumentDTO document;
      InputStream s3FileContent;
      String key = configuration.getS3EnhancerInput() + DocumentType.IMAGE.name().toLowerCase() + "s";
      List<S3ObjectSummary> objectsForEnhancement = getDocumentsForEnhancement(s3Client.listObjects(configuration.getS3Bucket(), key));
      logger.info("enhanceImages - there are " + objectsForEnhancement.size() + " images to be enhanced");
      for (S3ObjectSummary s3ObjectSummary : objectsForEnhancement) {
         // Ignore folders
         if (!s3ObjectSummary.getKey().endsWith("/")) {

            document = new DocumentDTO();
            document.docType = DocumentType.IMAGE.name();
            document.title = getFileNameFromS3ObjectSummary(s3ObjectSummary);
            document.origin = configuration.getS3Bucket() + ":" + s3ObjectSummary.getKey();

            // The cached imaged metadata will be used for enhancement (if exists)
            document.content = documentService.readImageMetadata(document.origin);

            // The image metadata will be extracted and used for enhancement
            if (document.content == null) {
               s3FileContent = s3Client.getFile(s3ObjectSummary.getBucketName(), s3ObjectSummary.getKey());
               document.content = document.title + "\n" + getImageMetadataAsString(s3FileContent);
            }

            try {
               enhance(document);
            } catch (Throwable e) {
               logger.error("enhanceImages - error processing file key: " + s3ObjectSummary.getKey());
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
