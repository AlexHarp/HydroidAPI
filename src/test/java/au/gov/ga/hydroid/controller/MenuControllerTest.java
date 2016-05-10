package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.MenuDTO;
import au.gov.ga.hydroid.utils.HydroidException;
import com.amazonaws.util.json.JSONArray;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.SKOS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HydroidApplication.class)
public class MenuControllerTest {

   private MockMvc mockMvc;

   @Autowired
   HydroidConfiguration configuration;

   @Autowired
   MenuController menuController;

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
      mockMvc = MockMvcBuilders.standaloneSetup(menuController).build();
   }

   @Test
   public void testHydroidMenu() {
      try {

         int x = 0, y = 0;

         Gson gson = new Gson();
         String menuJson = IOUtils.toString(getClass().getResourceAsStream("/hydroid-menu.json"));
         Model model = ModelFactory.createDefaultModel().read("hydroid.rdf");
         ResIterator resources = model.listResourcesWithProperty(null);
         List<MenuDTO> list = gson.fromJson(menuJson, new TypeToken<List<MenuDTO>>(){}.getType());

         while(resources.hasNext()) {
             if(resources.next().hasProperty(SKOS.topConceptOf)) x++;
         }
         for(MenuDTO item : list) {
             if (item.getChildren().size() > 0) y += item.getChildren().size();
         }

         Assert.assertSame(y, x);

         this.mockMvc.perform(
                 MockMvcRequestBuilders.get("/menu/hydroid")
                         .contentType(MediaType.APPLICATION_JSON)
                         .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().string(menuJson));

      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }
}