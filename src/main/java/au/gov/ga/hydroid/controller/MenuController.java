package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.dto.MenuDTO;
import au.gov.ga.hydroid.utils.HydroidException;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuController {

   @RequestMapping(value = "/hydroid", method = {RequestMethod.GET})
   public @ResponseBody ResponseEntity<List<MenuDTO>> enhance() {

       MenuDTO menuItem, childMenuItem;
       List<MenuDTO> menu = new ArrayList<>();

       try {
           Model model = ModelFactory.createDefaultModel();
           model.read("hydroid.rdf");
           ResIterator parentResources = model.listResourcesWithProperty(RDF.type);

           while (parentResources.hasNext()) {
               Resource parentRes = parentResources.nextResource();

               if (parentRes.hasProperty(RDFS.label)) {
                   Statement resStmt = parentRes.getProperty(RDFS.label);

                   menuItem = new MenuDTO();
                   menuItem.setNodeLabel(resStmt.getString());
                   menuItem.setNodeURI(resStmt.getSubject().getURI());

                   List<MenuDTO> childMenu = new ArrayList<>();
                   ResIterator childResources = model.listResourcesWithProperty(SKOS.topConceptOf);

                   while (childResources.hasNext()) {
                       Resource childRes = childResources.next();

                       String parentRefURI = childRes.getProperty(SKOS.topConceptOf).getObject().toString();
                       String parentURI = resStmt.getSubject().getURI();

                       if (parentRefURI.equals(parentURI)) {
                           Statement childStmt = childRes.getProperty(SKOS.prefLabel);
                           childMenuItem = new MenuDTO();
                           childMenuItem.setNodeLabel(childStmt.getString());
                           childMenuItem.setNodeURI(childStmt.getSubject().getURI());
                           childMenu.add(childMenuItem);
                       }
                   }
                   menuItem.setChildren(childMenu);
                   menu.add(menuItem);
               }
           }
       } catch (Exception e) {
           throw new HydroidException(e);
       }
       return new ResponseEntity<>(menu, HttpStatus.OK);
   }
}
