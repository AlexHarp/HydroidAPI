package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.DocumentService;
import au.gov.ga.hydroid.service.JenaService;
import au.gov.ga.hydroid.service.SolrClient;
import au.gov.ga.hydroid.utils.HydroidException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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

   private ClearAllController clearAllController;

   @Autowired
   private HydroidConfiguration configuration;

   @Mock
   private SolrClient solrClient;

   @Mock
   private JenaService jenaService;

   @Mock
   private DocumentService documentService;

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
      clearAllController = new ClearAllController();
      ReflectionTestUtils.setField(clearAllController, "configuration", configuration);
      ReflectionTestUtils.setField(clearAllController, "solrClient", solrClient);
      ReflectionTestUtils.setField(clearAllController, "jenaService", jenaService);
      ReflectionTestUtils.setField(clearAllController, "documentService", documentService);
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