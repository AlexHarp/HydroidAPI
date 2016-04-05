package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.DocumentDTO;
import au.gov.ga.hydroid.dto.ServiceResponse;
import au.gov.ga.hydroid.job.EnhancerJob;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.service.EnhancerService;
import au.gov.ga.hydroid.utils.HydroidException;
import au.gov.ga.hydroid.utils.IOUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.tika.metadata.Metadata;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

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
         logger.debug("validateDocType - docType is not valid");
         return false;
      }
   }

   private String validateDocument(DocumentDTO document) {
      if (document == null || document.content == null || document.docType == null) {
         return "Please enter the text/content and document type for enhancement.";
      }
      if (!validateDocType(document.docType)) {
         return "Document.type is invalid, it must be one of DOCUMENT, DATASET OR MODEL.";
      }
      return null;
   }

   @RequestMapping(value = "", method = {RequestMethod.POST})
   public @ResponseBody ResponseEntity<ServiceResponse> enhance(@RequestBody DocumentDTO document) {
      try {

         String errorMessage = validateDocument(document);
         if (errorMessage != null) {
            return new ResponseEntity<>(new ServiceResponse(errorMessage),
                  HttpStatus.BAD_REQUEST);

         }

         document.origin = "Manual Enhancement/UI";
         document.dateCreated = new Date();
         enhancerService.enhance(document);

      } catch (Exception e) {
         logger.error("enhance - Exception: ", e);
         return new ResponseEntity<>(new ServiceResponse("There has been an error enhancing your document, please try again later.",
               e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
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
            document.content = IOUtils.parseFile(byteArrayInputStream, metadata);
            document.docType = DocumentType.DOCUMENT.name();
            document.author = metadata.get("author") == null ? metadata.get("Author") : metadata.get("author");
            document.title = metadata.get("title") == null ? name : metadata.get("title");
            document.origin = configuration.getS3Bucket() + ":" + configuration.getS3EnhancerInput() + DocumentType.DOCUMENT.name().toLowerCase() + "s/" + name;
            document.dateCreated = metadata.get("Creation-Date") == null ? null :
                  DateUtils.parseDate(metadata.get("Creation-Date"), new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"});

            enhancerService.enhance(document);
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

   private boolean checkAndTriggerJob(SchedulerFactoryBean schedulerFactoryBean) throws HydroidException {

      if (schedulerFactoryBean == null) {
         return false;
      }

      try {
         Scheduler scheduler = schedulerFactoryBean.getScheduler();

         // Check if any jobs are currently running
         List<JobExecutionContext> jobs = schedulerFactoryBean.getScheduler().getCurrentlyExecutingJobs();
         if (jobs != null && !jobs.isEmpty()) {
            for (JobExecutionContext job : jobs) {
               if (job.getJobDetail().getJobClass().equals(EnhancerJob.class)) {
                  return true;
               }
            }
            // if not trigger job manually
         } else {
            JobDetail jobDetail = (JobDetail) context.getBean("enhancerJobDetail");
            if (jobDetail != null) {
               scheduler.triggerJob(jobDetail.getKey());
            }
         }

      } catch (Exception e) {
         logger.error("checkAndTriggerJob - Exception: ", e);
         throw new HydroidException(e);
      }

      return false;
   }

   @RequestMapping(value = "/s3", method = {RequestMethod.GET, RequestMethod.POST})
   public @ResponseBody ResponseEntity<ServiceResponse> enhanceS3() {

      SchedulerFactoryBean schedulerFactoryBean = context.getBean(SchedulerFactoryBean.class);
      if (checkAndTriggerJob(schedulerFactoryBean)) {
         return new ResponseEntity<>(new ServiceResponse("The enhancement process is currently in progress, try again later."),
               HttpStatus.OK);
      }

      return new ResponseEntity<>(new ServiceResponse("The enhancement process has started successfully."),
            HttpStatus.OK);

   }

}
