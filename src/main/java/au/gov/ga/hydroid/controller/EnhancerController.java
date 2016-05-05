package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.SchedulerFactory;
import au.gov.ga.hydroid.dto.DocumentDTO;
import au.gov.ga.hydroid.dto.ServiceResponse;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.service.EnhancerService;
import au.gov.ga.hydroid.utils.IOUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Date;

/**
 * Created by u24529 on 3/02/2016.
 */
@RestController
@RequestMapping("/enhancer")
public class EnhancerController {

   private Logger logger = LoggerFactory.getLogger(getClass());

   @Autowired
   private EnhancerService enhancerService;

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   private ApplicationContext context;

   private boolean validateDocType(String docType) {
      try {
         DocumentType.valueOf(docType);
         return true;
      } catch (Exception e) {
         logger.debug("validateDocType - Exception: ", e);
         return false;
      }
   }

   private String validateDocument(DocumentDTO document) {
      if (document == null || document.getContent() == null || document.getDocType() == null) {
         return "Please enter the text/content and document type for enhancement.";
      }
      if (!validateDocType(document.getDocType())) {
         return "Document.type is invalid, it must be one of DOCUMENT, DATASET OR MODEL.";
      }
      return null;
   }

   @RequestMapping(value = "", method = {RequestMethod.POST})
   public @ResponseBody ResponseEntity<ServiceResponse> enhance(@RequestBody DocumentDTO document) {

      String errorMessage = validateDocument(document);
      if (errorMessage != null) {
         return new ResponseEntity<>(new ServiceResponse(errorMessage),
               HttpStatus.BAD_REQUEST);

      }

      document.setOrigin("Manual Enhancement/UI");
      document.setDateCreated(new Date());
      if (!enhancerService.enhance(document)) {
         return new ResponseEntity<>(
               new ServiceResponse("There has been an error enhancing your document, please try again later."),
               HttpStatus.OK);
      }

      return new ResponseEntity<>(new ServiceResponse("Your document has been enhanced successfully."),
            HttpStatus.OK);
   }

   @RequestMapping(value="/file", method = {RequestMethod.POST})
   public @ResponseBody
   ResponseEntity<ServiceResponse> enhanceFile(@RequestParam("name") String name,
                                               @RequestParam("file") MultipartFile file) {
      if (!file.isEmpty()) {
         try {
            byte[] bytes = file.getBytes();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            Metadata metadata = new Metadata();
            DocumentDTO document = new DocumentDTO();
            document.setContent(IOUtils.parseStream(byteArrayInputStream, metadata));
            document.setDocType(DocumentType.DOCUMENT.name());
            document.setAuthor(metadata.get("author") == null ? metadata.get("Author") : metadata.get("author"));
            document.setTitle(metadata.get("title") == null ? name : metadata.get("title"));
            document.setOrigin(configuration.getS3Bucket() + ":" + configuration.getS3EnhancerInput() + DocumentType.DOCUMENT.name().toLowerCase() + "s/" + name);
            document.setDateCreated(metadata.get("Creation-Date") == null ? null :
                  DateUtils.parseDate(metadata.get("Creation-Date"), new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"}));

            if (!enhancerService.enhance(document)) {
               return new ResponseEntity<>(
                     new ServiceResponse("There has been an error enhancing your document, please try again later."),
                     HttpStatus.OK);
            }

         } catch (Exception e) {
            logger.warn("enhanceFile - Exception: ", e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed extracting/indexing text from file");
         }
      } else {
         return new ResponseEntity<>(
               new ServiceResponse("You failed to upload " + name + " because the file was empty."), HttpStatus.OK);
      }

      return new ResponseEntity<>(new ServiceResponse("Your document has been enhanced successfully."),
            HttpStatus.OK);
   }

   private SchedulerFactory getSchedulerFactory() {
      try {
         return context.getBean(SchedulerFactory.class);
      } catch (BeansException e) {
         logger.debug("getSchedulerFactory - BeansException: ", e);
         return null;
      }
   }

   @RequestMapping(value = "/s3", method = {RequestMethod.GET, RequestMethod.POST})
   public @ResponseBody ResponseEntity<ServiceResponse> enhanceS3() {

      SchedulerFactory schedulerFactory = getSchedulerFactory();
      if (schedulerFactory == null) {
         return new ResponseEntity<>(new ServiceResponse("The enhancement process is currently disabled, try again later."),
               HttpStatus.OK);
      }

      if (schedulerFactory.isThereAnyJobRunning()) {
         return new ResponseEntity<>(new ServiceResponse("The enhancement process is currently in progress, try again later."),
               HttpStatus.OK);
      }

      schedulerFactory.triggerJob();
      return new ResponseEntity<>(new ServiceResponse("The enhancement process has started successfully."),
            HttpStatus.OK);

   }

}
