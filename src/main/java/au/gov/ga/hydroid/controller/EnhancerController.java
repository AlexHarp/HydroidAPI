package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.service.EnhancerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by u24529 on 3/02/2016.
 */
@RestController
@RequestMapping("/enhancer")
public class EnhancerController {

   private Logger logger = LoggerFactory.getLogger(getClass());

   @Autowired
   private EnhancerService enhancerService;

   @RequestMapping(value = "", method = {RequestMethod.GET})
   public ModelAndView index() throws Exception {
      return new ModelAndView("enhance.html");
   }

   @RequestMapping(value = "", method = {RequestMethod.POST})
   public ModelAndView enhance(@ModelAttribute Document document) throws Exception {

      Map<String, String> model = new HashMap<String, String>();

      if (document == null || document.getContent() == null) {
         model.put("alert-css", "alert alert-danger");
         model.put("alert-message", "Please enter the text/content for enhancement.");
         return new ModelAndView("index", model);
      }

      try {
         enhancerService.enhance("default", new String(document.getContent()), "hydroid");
         model.put("alert-css", "alert alert-success");
         model.put("alert-message", "Your document has been enhanced successfully.");
      } catch (Exception e) {
         logger.error("enhance - Exception: ", e);
         model.put("alert-css", "alert alert-danger");
         model.put("alert-message", "There has been an error enhancing your document, please try again later.");
      }

      return new ModelAndView("index", model);
   }

}
