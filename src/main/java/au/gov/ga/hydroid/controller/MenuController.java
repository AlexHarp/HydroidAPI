package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.dto.MenuDTO;
import au.gov.ga.hydroid.service.JenaService;
import au.gov.ga.hydroid.utils.HydroidException;
import au.gov.ga.hydroid.utils.IOUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.VCARD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by u24529 on 15/04/2016.
 */
@RestController
@RequestMapping("/menu")
public class MenuController {
    private Logger logger = LoggerFactory.getLogger(getClass());

   @Autowired
   private JenaService jenaService;

   @RequestMapping(value = "/hydroid", method = {RequestMethod.GET})
   public @ResponseBody ResponseEntity<List<MenuDTO>> enhance() {

       MenuDTO menuItem;
       List<MenuDTO> menu = new ArrayList<MenuDTO>();

       try {

           InputStream inputStream = getClass().getResourceAsStream("/hydroid.rdf");
           String rdfContent =  new String(IOUtils.fromInputStreamToByteArray(inputStream));
           List<Statement> statements = jenaService.parseRdf(rdfContent, "");

           for(Statement statement : statements) {
               menuItem = new MenuDTO();

               if (statement.getPredicate().toString().toUpperCase().contains("LABEL")) {
                   menuItem.setNodeURI(statement.getSubject().getURI());
                   menuItem.setNodeLabel(statement.getString());
                   menu.add(menuItem);
               }
           }

       } catch (Exception e) {
           throw new HydroidException(e);
       }

      return new ResponseEntity<>(menu, HttpStatus.OK);
   }
}
