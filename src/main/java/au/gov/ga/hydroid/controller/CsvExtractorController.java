package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.dto.ServiceResponse;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.model.EnhancementStatus;
import au.gov.ga.hydroid.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/import")
public class CsvExtractorController {
   private Logger logger = LoggerFactory.getLogger(getClass());

   @Autowired
   private DocumentService documentService;

   @RequestMapping(value="/bulk", method = {RequestMethod.POST})
   public @ResponseBody
   ResponseEntity<ServiceResponse> enhanceFile(@RequestParam("name") String name,
                                               @RequestParam("file") MultipartFile file) {
      if (!file.isEmpty()) {
         try {
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            while ((line = br.readLine()) != null) {
               String url = line.split(",")[0];
               savePendingDocument(url);
            }
         } catch (Throwable e) {
            logger.error("Failed to get URL from CSV: " + name,e);
         }
      } else {
         return new ResponseEntity<>(
                 new ServiceResponse("You failed to upload " + name + " because the file was empty."), HttpStatus.OK);
      }

      return new ResponseEntity<>(new ServiceResponse("Your document has queued for enhancement successfully."),
              HttpStatus.OK);
   }

   private void savePendingDocument(String url) {
      if (documentService.findByOrigin(url) == null) {
         Document document = new Document();
         document.setOrigin(url);
         document.setTitle(url);
         document.setType(DocumentType.DOCUMENT);
         document.setStatus(EnhancementStatus.PENDING);
         documentService.create(document);
      }
   }

}
