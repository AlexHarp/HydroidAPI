package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.service.EnhancerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by u24529 on 3/02/2016.
 */
@Controller
@RequestMapping("/enhancer")
public class EnhancerController {

   private Logger logger = LoggerFactory.getLogger(getClass());

   @Autowired
   private EnhancerService enhancerService;

   @RequestMapping(value = "", method = {RequestMethod.GET})
   public String index(Map<String, String> model) throws Exception {
      model.clear();
      return "enhance";
   }

   @RequestMapping(value = "", method = {RequestMethod.POST})
   public ModelAndView enhance(@ModelAttribute Document document) throws Exception {

      Map<String, String> model = new HashMap<String, String>();

      if (document == null || document.getContent() == null || document.getContent().length == 0) {
         model.put("alertCss", "alert alert-danger");
         model.put("alertMessage", "Please enter the text/content for enhancement.");
         return new ModelAndView("enhance", model);
      }

      try {
         enhancerService.enhance("default", new String(document.getContent()), "hydroid");
         model.put("alertCss", "alert alert-success");
         model.put("alertMessage", "Your document has been enhanced successfully.");
      } catch (Exception e) {
         logger.error("enhance - Exception: ", e);
         model.put("alertCss", "alert alert-danger");
         model.put("alertMessage", "There has been an error enhancing your document, please try again later.");
      }

      return new ModelAndView("enhance", model);
   }

}
