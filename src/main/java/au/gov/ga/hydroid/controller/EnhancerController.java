package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.DocumentDTO;
import au.gov.ga.hydroid.dto.ServiceResponse;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.service.EnhancerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

   @RequestMapping(value = "", method = {RequestMethod.POST})
   public @ResponseBody ResponseEntity<ServiceResponse> enhance(@RequestBody DocumentDTO document) throws Exception {

      if (document == null || document.content == null || document.content.length() == 0) {
         return new ResponseEntity<ServiceResponse>(new ServiceResponse("Please enter the text/content for enhancement."),
               HttpStatus.BAD_REQUEST);
      }

      try {
         if (!validateDocType(document.docType)) {
            return new ResponseEntity<ServiceResponse>(new ServiceResponse("Document.type is invalid, it must be one of DOCUMENT, DATASET OR MODEL."),
                  HttpStatus.BAD_REQUEST);

         }
         enhancerService.enhance(document.title, document.content, document.docType);
      } catch (Exception e) {
         logger.error("enhance - Exception: ", e);
         return new ResponseEntity<ServiceResponse>(new ServiceResponse("There has been an error enhancing your document, please try again later.",
               e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
      }

      return new ResponseEntity<ServiceResponse>(new ServiceResponse("Your document has been enhanced successfully."),
            HttpStatus.OK);

   }

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
         default:
            return false;
      }
   }

}
