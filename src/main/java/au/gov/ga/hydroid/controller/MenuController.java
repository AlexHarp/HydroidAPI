package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.dto.MenuDTO;
import au.gov.ga.hydroid.utils.HydroidException;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.SortedSet;
import java.util.TreeSet;

@RestController
@RequestMapping("/menu")
public class MenuController {

   private static final Logger logger = LoggerFactory.getLogger(MenuController.class);
   private static SortedSet<MenuDTO> hydroidMenu;

   private SortedSet<MenuDTO> buildMenu(String rdfName) {

      SortedSet<MenuDTO> menu = new TreeSet<>();
      MenuDTO menuItem, childMenuItem;


      try {

         Model model = ModelFactory.createDefaultModel();
         model.read(rdfName);
         ResIterator resources = model.listResourcesWithProperty(RDF.type);

         while (resources.hasNext()) {
            Resource parentRes = resources.nextResource();

            if (parentRes.hasProperty(RDFS.label)) {
               Statement resStmt = parentRes.getProperty(RDFS.label);

               menuItem = new MenuDTO();
               menuItem.setNodeLabel(resStmt.getString());
               menuItem.setNodeURI(resStmt.getSubject().getURI());

               ResIterator childResources = model.listResourcesWithProperty(SKOS.topConceptOf);

               while (childResources.hasNext()) {
                  Resource childRes = childResources.next();

                  String childURI = childRes.getProperty(SKOS.topConceptOf).getObject().toString();
                  String parentURI = resStmt.getSubject().getURI();

                  if (childURI.equals(parentURI)) {
                     Statement childResStmt = childRes.getProperty(SKOS.prefLabel);
                     childMenuItem = new MenuDTO();
                     childMenuItem.setNodeLabel(childResStmt.getString());
                     childMenuItem.setNodeURI(childResStmt.getSubject().getURI());
                     menuItem.getChildren().add(childMenuItem);
                  }
               }
               menu.add(menuItem);
            }
         }
      } catch (Exception e) {
         throw new HydroidException(e);
      }

      System.out.println("Done!");

      return menu;
   }

   @RequestMapping(value = "/hydroid", method = {RequestMethod.GET})
   public @ResponseBody ResponseEntity<SortedSet<MenuDTO>> hydroid() {
      if (hydroidMenu == null) {
         hydroidMenu = buildMenu("hydroid.rdf");
      }
      return new ResponseEntity<>(hydroidMenu, HttpStatus.OK);
   }

}
