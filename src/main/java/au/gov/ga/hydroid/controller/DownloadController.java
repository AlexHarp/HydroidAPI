package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.service.DocumentService;
import au.gov.ga.hydroid.service.S3Client;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.jboss.resteasy.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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

   private MediaType getMediaType(byte[] content, MediaType fallBackMediaType) {
      MediaType mediaType;
      try {
         mediaType = MediaType.valueOf(new Tika().detect(content));
      } catch (Exception e) {
         logger.debug("getMediaType - Exception: ", e);
         mediaType = fallBackMediaType;
      }
      return mediaType;
   }
   
   private HttpHeaders getHttpHeaders(MediaType mediaType, long length, String fileName) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(mediaType);
      headers.setContentLength(length);
      headers.setContentDispositionFormData("attachment", fileName);
      return headers;
   }

   private ResponseEntity<byte[]> donwloadSingle(String bucket, String key, String fileName,
                                                 MediaType fallBackMediaType) {
      try {

         byte[] fileContent = s3Client.getFileAsByteArray(bucket, key);
         if (fileContent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
         }

         MediaType mediaType = getMediaType(fileContent, fallBackMediaType);
         HttpHeaders headers = getHttpHeaders(mediaType, fileContent.length, fileName);
         return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);

      } catch (Exception e) {
         logger.error("downloadSingle - Exception: ", e);
         return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
   }

   @RequestMapping(value = "/rdfs/{urn}", method = {RequestMethod.GET})
   public @ResponseBody ResponseEntity<byte[]> downloadRDF(@PathVariable String urn) {
      return donwloadSingle(configuration.getS3OutputBucket(), configuration.getS3EnhancerOutput() + urn, urn,
            MediaType.APPLICATION_XML);
   }

   @RequestMapping(value = "/documents/{urn}", method = {RequestMethod.GET})
   public @ResponseBody ResponseEntity<byte[]> downloadDocument(@PathVariable String urn) {
      Document document = documentService.findByUrn(urn);
      if (document == null) {
         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      String[] bucketAndKey = document.getOrigin().split(":");
      String fileName = bucketAndKey[1].substring(bucketAndKey[1].lastIndexOf("/") + 1);
      return donwloadSingle(bucketAndKey[0], bucketAndKey[1], fileName, MediaType.APPLICATION_OCTET_STREAM);
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
   public @ResponseBody ResponseEntity<byte[]> downloadBundle(@PathVariable String urnList) {
      try {
         String[] urnArray = urnList.split(",");
         String outputFileName = "rdfs-bundle-" + DateUtil.formatDate(new Date(), "yyyyMMddHHmm") + ".zip";

         // Generate full zip with all <urn>.rdf files
         File zipFile = File.createTempFile(outputFileName, null);
         ZipOutputStream zipOut  = new ZipOutputStream(new FileOutputStream(zipFile));
         int filesAdded = addFilesToBundle(urnArray, zipOut);
         zipOut.close();

         if (filesAdded == 0) {
            return new ResponseEntity<>("No files were found or bundled for download.".getBytes(),
                  HttpStatus.OK);
         }

         byte[] zipContent = IOUtils.toByteArray(new FileInputStream(zipFile));
         HttpHeaders headers = getHttpHeaders(MediaType.APPLICATION_OCTET_STREAM, zipContent.length,
               outputFileName);

         return new ResponseEntity<>(zipContent, headers, HttpStatus.OK);

      } catch (Exception e) {
         logger.error("downloadBundle - Exception: ", e);
         return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
   }

   @RequestMapping(value = "/images/{urn}", method = {RequestMethod.GET})
   public@ResponseBody ResponseEntity<byte[]> downloadImage(@PathVariable String urn) {
      String urnNoThumb = urn;
      if (urn.contains("_thumb")) {
         urnNoThumb = urnNoThumb.replace("_thumb", "");
      }
      Document document = documentService.findByUrn(urnNoThumb);
      if (document == null) {
         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      String fileName = document.getOrigin().substring(document.getOrigin().lastIndexOf("/") + 1);
      return donwloadSingle(configuration.getS3OutputBucket(), configuration.getS3EnhancerOutputImages() + urn,
            fileName, MediaType.APPLICATION_OCTET_STREAM);
   }

}
