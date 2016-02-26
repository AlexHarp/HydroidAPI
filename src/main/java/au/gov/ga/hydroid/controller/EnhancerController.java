package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.DocumentDTO;
import au.gov.ga.hydroid.dto.ServiceResponse;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.service.EnhancerService;
import au.gov.ga.hydroid.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

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

   private boolean validateDocType(String docType) {
      if (docType == null || docType.isEmpty()) {
         return true;
      }
      DocumentType enumDocType = DocumentType.valueOf(docType);
      switch (enumDocType) {
         case DOCUMENT:
            return true;
         case DATASET:
            return true;
         case MODEL:
            return true;
         case IMAGE:
            return true;
         default:
            return false;
      }
   }

   @RequestMapping(value = "", method = {RequestMethod.POST})
   public @ResponseBody ResponseEntity<ServiceResponse> enhance(@RequestBody DocumentDTO document) {

      if (document == null || document.content == null || document.content.length() == 0) {
         return new ResponseEntity<ServiceResponse>(new ServiceResponse("Please enter the text/content for enhancement."),
               HttpStatus.BAD_REQUEST);
      }

      try {
         if (!validateDocType(document.docType)) {
            return new ResponseEntity<ServiceResponse>(new ServiceResponse("Document.type is invalid, it must be one of DOCUMENT, DATASET OR MODEL."),
                  HttpStatus.BAD_REQUEST);

         }
         String origin = new StringBuilder(configuration.getS3Bucket()).append(":").append(configuration.getS3EnhancerInput())
               .append(document.docType.toLowerCase()).append("s/").append(document.title).toString();

         enhancerService.enhance(document.title, document.content, document.docType, origin);
      } catch (Throwable e) {
         logger.error("enhance - Exception: ", e);
         return new ResponseEntity<ServiceResponse>(new ServiceResponse("There has been an error enhancing your document, please try again later.",
               e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
      }

      return new ResponseEntity<ServiceResponse>(new ServiceResponse("Your document has been enhanced successfully."),
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
            String text = IOUtils.parseFile(byteArrayInputStream);
            String origin = new StringBuilder(configuration.getS3Bucket()).append(":").append(configuration.getS3EnhancerInput())
                  .append(DocumentType.DOCUMENT.name().toLowerCase()).append("s/").append(name).toString();
            enhancerService.enhance(name, text, DocumentType.DOCUMENT.name(), origin);
         } catch (Throwable e) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed extracting/indexing text from file");
         }
      } else {
         return new ResponseEntity<ServiceResponse>(
               new ServiceResponse("You failed to upload " + name + " because the file was empty."), HttpStatus.OK);
      }

      return new ResponseEntity<ServiceResponse>(new ServiceResponse("Your document has been enhanced successfully."),
            HttpStatus.OK);
   }

   @RequestMapping(value = "/s3", method = {RequestMethod.POST})
   public @ResponseBody ResponseEntity<ServiceResponse> enhanceS3() {

      enhancerService.enhanceDocuments();
      enhancerService.enhanceDatasets();
      enhancerService.enhanceModels();
      enhancerService.enhanceImages();

      return new ResponseEntity<ServiceResponse>(new ServiceResponse("Your documents have been enhanced successfully."),
            HttpStatus.OK);

   }

}
