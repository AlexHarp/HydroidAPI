package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.service.DocumentService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;

/**
 * Created by u24529 on 4/02/2016.
 */
@RestController
@RequestMapping("/document")
public class DocumentController {

   private Logger logger = LoggerFactory.getLogger(getClass());

   @Autowired
   private DocumentService documentService;

   @RequestMapping(value = "/{urn}/download", method = {RequestMethod.GET})
   public @ResponseBody String download(@PathVariable String urn, HttpServletResponse response) throws Exception {

      try {
         Document document = documentService.findByUrn(urn);
         if(document == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return "";
         }
         response.setHeader("Content-Disposition", "attachment; filename=\"" + document.getUrn() + "\"");
         response.setContentLength(document.getContent().length);

         ByteArrayInputStream bais = new ByteArrayInputStream(document.getContent());
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

}
