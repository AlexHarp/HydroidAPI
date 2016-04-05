package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.DocumentDTO;
import au.gov.ga.hydroid.service.EnhancerService;
import au.gov.ga.hydroid.utils.HydroidException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HydroidApplication.class)
public class EnhancerControllerTest {    private MockMvc mockMvc;

   @Autowired
   HydroidConfiguration configuration;

   @Mock
   EnhancerService enhancerService;

   @Mock
   ApplicationContext context;

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
      EnhancerController enhancerController = new EnhancerController();
      ReflectionTestUtils.setField(enhancerController, "enhancerService", this.enhancerService);
      ReflectionTestUtils.setField(enhancerController, "configuration", this.configuration);
      ReflectionTestUtils.setField(enhancerController, "context", this.context);
      mockMvc = MockMvcBuilders.standaloneSetup(enhancerController).build();
   }

   @Test
   public void testEnhance() {
      try {
         DocumentDTO request = new DocumentDTO();
         request.content = "foo";
         request.title = "bar";
         ObjectMapper mapper = new ObjectMapper();
         String json = mapper.writeValueAsString(request);
         this.mockMvc.perform(
               MockMvcRequestBuilders.post("/enhancer")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON)
                     .content(json))
               .andExpect(status().isOk());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testUploadFile() {
      try {
         InputStream is = this.getClass().getResourceAsStream("/testfiles/Creativecommons-informational-flyer_eng.pdf");
         byte[] fileBytes = IOUtils.toByteArray(is);
         MockMultipartFile firstFile = new MockMultipartFile("file",
               "Creativecommons-informational-flyer_eng.pdf",
               "application/pdf", fileBytes);
         this.mockMvc.perform(fileUpload("/enhancer/file").file(firstFile).param("name", "mypdf.pdf"))
               .andExpect(status().isOk());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testEnhanceS3() {
      try {
         this.mockMvc.perform(
               MockMvcRequestBuilders.post("/enhancer/s3")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

}