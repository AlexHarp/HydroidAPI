package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.S3Client;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import org.apache.commons.io.IOUtils;
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
@RequestMapping("/document")
public class DocumentController {

   private Logger logger = LoggerFactory.getLogger(getClass());

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   private S3Client s3Client;

   @RequestMapping(value = "/{urn}/download", method = {RequestMethod.GET})
   public @ResponseBody String download(@PathVariable String urn, HttpServletResponse response) throws Exception {

      try {
         byte[] document = s3Client.getFile(configuration.getS3Bucket(), configuration.getS3RDFFolder() + urn);
         if (document == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return "";
         }
         response.setHeader("Content-Disposition", "attachment; filename=\"" + urn + ".rdf\"");
         response.setContentLength(document.length);
         response.setContentType(StanbolMediaTypes.RDFXML.toString());

         ByteArrayInputStream bais = new ByteArrayInputStream(document);
         OutputStream out = response.getOutputStream();
         bais.mark(0);
         IOUtils.copyLarge(bais, out);

         out.flush();
         out.close();

      } catch (EmptyResultDataAccessException e) {
         response.sendError(HttpServletResponse.SC_NOT_FOUND);
      } catch (Throwable e) {
         logger.error("download - Exception: ", e);
         response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }

      return null;
   }

   @RequestMapping(value = "/{urnList}/download-bundle", method = {RequestMethod.GET})
   public @ResponseBody String downloadBundle(@PathVariable String urnList, HttpServletResponse response) throws Exception {

      try {
         String[] urnArray = urnList.split(",");
         String outputFileName = "rdfs-bundle-" + DateUtil.formatDate(new Date(), "yyyyMMdd") + ".zip";

         // Generate full zip with all <urn>.rdf files
         File zipFile = File.createTempFile(outputFileName, null);
         ZipOutputStream zipOut  = new ZipOutputStream(new FileOutputStream(zipFile));
         byte[] buffer = new byte[1024];
         for (String urn : urnArray) {
            byte[] document = s3Client.getFile(configuration.getS3Bucket(), configuration.getS3RDFFolder() + urn);
            if (document != null) {
               try {
                  InputStream in = new ByteArrayInputStream(document);
                  zipOut.putNextEntry(new ZipEntry(urn + ".rdf"));

                  int length;
                  while ((length = in.read(buffer)) > 0) {
                     zipOut.write(buffer, 0, length);
                  }

                  zipOut.closeEntry();
                  in.close();

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

      } catch (Throwable e) {
         logger.error("downloadBundle - Exception: ", e);
         response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }

      return null;
   }

}
