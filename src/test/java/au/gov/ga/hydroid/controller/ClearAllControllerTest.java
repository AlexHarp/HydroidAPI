package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.mock.CustomMockDocumentService;
import au.gov.ga.hydroid.mock.CustomMockJenaService;
import au.gov.ga.hydroid.mock.CustomMockSolrClient;
import au.gov.ga.hydroid.utils.HydroidException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HydroidApplication.class)
public class ClearAllControllerTest {

   private MockMvc mockMvc;

   @Autowired
   private HydroidConfiguration configuration;

   private ClearAllController clearAllController;

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
      clearAllController = new ClearAllController();
      ReflectionTestUtils.setField(clearAllController, "configuration", this.configuration);
      ReflectionTestUtils.setField(clearAllController, "solrClient", new CustomMockSolrClient());
      ReflectionTestUtils.setField(clearAllController, "jenaService", new CustomMockJenaService());
      ReflectionTestUtils.setField(clearAllController, "documentService", new CustomMockDocumentService());
      mockMvc = MockMvcBuilders.standaloneSetup(clearAllController).build();
   }

   @Test
   public void testReset_NotFound() {
      try {
         this.mockMvc.perform(
               MockMvcRequestBuilders.get("/reset")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testReset_Success() {
      try {
         this.mockMvc.perform(
               MockMvcRequestBuilders.post("/reset/all")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().string("Success"));
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

}