package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.service.DocumentService;
import au.gov.ga.hydroid.service.S3Client;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.jboss.resteasy.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by u24529 on 4/02/2016.
 */
@RestController
@RequestMapping("/download")
public class DownloadController {

   private static Logger logger = LoggerFactory.getLogger(DownloadController.class);

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   @Value("#{systemProperties['s3.use.file.system'] != null ? s3FileSystem : s3ClientImpl}")
   private S3Client s3Client;

   @Autowired
   private DocumentService documentService;

   private ResponseEntity<byte[]> donwloadSingle(String bucket, String key, MediaType mediaType) {

      try {

         byte[] fileContent = s3Client.getFileAsByteArray(bucket, key);
         if (fileContent == null) {
            return new ResponseEntity<>(org.springframework.http.HttpStatus.NOT_FOUND);
         }

         String fileName = key.substring(key.lastIndexOf("/") + 1);

         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(org.springframework.http.MediaType.valueOf(mediaType.toString()));
         headers.setContentLength(fileContent.length);
         headers.setContentDispositionFormData("attachment", fileName);

         return new ResponseEntity<>(fileContent, headers, org.springframework.http.HttpStatus.OK);

      } catch (Exception e) {
         logger.error("downloadSingle - Exception: ", e);
         return new ResponseEntity<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
      }
   }

   @RequestMapping(value = "/rdfs/{urn}", method = {RequestMethod.GET})
   public @ResponseBody ResponseEntity<byte[]> downloadRDF(@PathVariable String urn) {
      return donwloadSingle(configuration.getS3OutputBucket(), configuration.getS3EnhancerOutput() + urn,
            StanbolMediaTypes.RDFXML);
   }

   @RequestMapping(value = "/documents/{urn}", method = {RequestMethod.GET})
   public @ResponseBody ResponseEntity<byte[]> downloadDocument(@PathVariable String urn) {
      Document document = documentService.findByUrn(urn);
      if (document == null) {
         return new ResponseEntity<>(org.springframework.http.HttpStatus.NOT_FOUND);
      }
      String[] bucketAndKey = document.getOrigin().split(":");
      return donwloadSingle(bucketAndKey[0], bucketAndKey[1], MediaType.APPLICATION_OCTET_STREAM_TYPE);
   }

   private int addFilesToBundle(String[] urnArray, ZipOutputStream zipOut) {
      int filesAdded = 0;

      for (String urn : urnArray) {
         int length;
         byte[] buffer = new byte[1024];
         try (InputStream fileContent = s3Client.getFile(configuration.getS3OutputBucket(),
               configuration.getS3EnhancerOutput() + urn)) {
            zipOut.putNextEntry(new ZipEntry(urn + ".rdf"));
            while ((length = fileContent.read(buffer)) > 0) {
               zipOut.write(buffer, 0, length);
            }
            zipOut.closeEntry();
            fileContent.close();
            filesAdded ++;
         } catch (Exception e) {
            logger.error("addFilesToBundle - Exception: ", e);
         }
      }

      return filesAdded;
   }

   @RequestMapping(value = "/bundle/{urnList}", method = {RequestMethod.GET})
   public @ResponseBody String downloadBundle(@PathVariable String urnList, HttpServletResponse response) {

      try {
         String[] urnArray = urnList.split(",");
         String outputFileName = "rdfs-bundle-" + DateUtil.formatDate(new Date(), "yyyyMMdd") + ".zip";

         // Generate full zip with all <urn>.rdf files
         File zipFile = File.createTempFile(outputFileName, null);
         ZipOutputStream zipOut  = new ZipOutputStream(new FileOutputStream(zipFile));
         int filesAdded = addFilesToBundle(urnArray, zipOut);
         zipOut.close();

         if (filesAdded == 0) {
            au.gov.ga.hydroid.utils.IOUtils.sendResponseError(response, HttpServletResponse.SC_OK);
            return "No files were found or bundled for download.";
         }

         // Write full zip file to output stream
         response.setHeader("Content-Disposition", "attachment; filename=\"" + outputFileName + "\"");
         response.setContentLength((int) zipFile.length());

         FileInputStream zipIn = new FileInputStream(zipFile);
         OutputStream out = response.getOutputStream();
         zipIn.mark(0);
         IOUtils.copyLarge(zipIn, out);
         out.flush();
         out.close();

         zipIn.close();
         zipFile.delete();

      } catch (Exception e) {
         logger.error("downloadBundle - Exception: ", e);
         au.gov.ga.hydroid.utils.IOUtils.sendResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }

      return null;
   }

   @RequestMapping(value = "/images/{urn}", method = {RequestMethod.GET})
   public @ResponseBody String downloadImage(@PathVariable String urn, HttpServletResponse response) {

      try {

         InputStream fileContent = s3Client.getFile(configuration.getS3OutputBucket(), configuration.getS3EnhancerOutputImages() + urn);
         if (fileContent == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
         }

         OutputStream out = response.getOutputStream();
         fileContent.mark(0);
         Long length = IOUtils.copyLarge(fileContent, out);

         response.setHeader("Content-Disposition", "inline; filename=\"" + urn + "\"");
         response.setContentLength(length.intValue());
         response.setContentType(ContentType.APPLICATION_OCTET_STREAM.toString());

         out.flush();
         out.close();

      } catch (Exception e) {
         logger.error("downloadImage - Exception: ", e);
         au.gov.ga.hydroid.utils.IOUtils.sendResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }

      return null;
   }

}
