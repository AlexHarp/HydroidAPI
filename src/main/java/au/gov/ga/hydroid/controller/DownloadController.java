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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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

   private Logger logger = LoggerFactory.getLogger(getClass());

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   private DocumentService documentService;

   @Autowired
   private S3Client s3Client;

   @RequestMapping(value = "/single/{urn}", method = {RequestMethod.GET})
   public @ResponseBody String downloadSingle(@PathVariable String urn, HttpServletResponse response) {

      try {

         InputStream fileContent = s3Client.getFile(configuration.getS3OutputBucket(), configuration.getS3EnhancerOutput() + urn);
         if (fileContent == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
         }

         OutputStream out = response.getOutputStream();
         fileContent.mark(0);
         Long length = IOUtils.copyLarge(fileContent, out);

         response.setHeader("Content-Disposition", "attachment; filename=\"" + urn + ".rdf\"");
         response.setContentLength(length.intValue());
         response.setContentType(StanbolMediaTypes.RDFXML.toString());

         out.flush();
         out.close();

      } catch (EmptyResultDataAccessException e) {
         au.gov.ga.hydroid.utils.IOUtils.sendResponseError(response, HttpServletResponse.SC_NOT_FOUND);
      } catch (Exception e) {
         logger.error("download - Exception: ", e);
         au.gov.ga.hydroid.utils.IOUtils.sendResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }

      return null;
   }

   @RequestMapping(value = "/bundle/{urnList}", method = {RequestMethod.GET})
   public @ResponseBody String downloadBundle(@PathVariable String urnList, HttpServletResponse response) {

      try {
         String[] urnArray = urnList.split(",");
         String outputFileName = "rdfs-bundle-" + DateUtil.formatDate(new Date(), "yyyyMMdd") + ".zip";

         // Generate full zip with all <urn>.rdf files
         File zipFile = File.createTempFile(outputFileName, null);
         ZipOutputStream zipOut  = new ZipOutputStream(new FileOutputStream(zipFile));
         byte[] buffer = new byte[1024];
         for (String urn : urnArray) {
            InputStream fileContent = s3Client.getFile(configuration.getS3OutputBucket(), configuration.getS3EnhancerOutput() + urn);
            if (fileContent != null) {
               try {
                  zipOut.putNextEntry(new ZipEntry(urn + ".rdf"));

                  int length;
                  while ((length = fileContent.read(buffer)) > 0) {
                     zipOut.write(buffer, 0, length);
                  }

                  zipOut.closeEntry();
                  fileContent.close();

               } catch (Exception e) {
                  logger.error("downloadBundle - Exception: ", e);
               }
            }
         }
         zipOut.close();

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

   @RequestMapping(value = "/image/{urn}", method = {RequestMethod.GET})
   public @ResponseBody String downloadImage(@PathVariable String urn, HttpServletResponse response) {

      try {

         Document document = documentService.findByUrn(urn);
         if (document == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
         }

         InputStream fileContent = s3Client.getFile(configuration.getS3OutputBucket(), configuration.getS3EnhancerOutputImages() + urn);
         if (fileContent == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
         }

         OutputStream out = response.getOutputStream();
         fileContent.mark(0);
         Long length = IOUtils.copyLarge(fileContent, out);

         response.setHeader("Content-Disposition", "inline; filename=\"" + document.getTitle() + "\"");
         response.setContentLength(length.intValue());
         response.setContentType(ContentType.APPLICATION_OCTET_STREAM.toString());

         out.flush();
         out.close();

      } catch (EmptyResultDataAccessException e) {
         au.gov.ga.hydroid.utils.IOUtils.sendResponseError(response, HttpServletResponse.SC_NOT_FOUND);
      } catch (Exception e) {
         logger.error("downloadImage - Exception: ", e);
         au.gov.ga.hydroid.utils.IOUtils.sendResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }

      return null;
   }

}
