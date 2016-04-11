package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.service.DocumentService;
import au.gov.ga.hydroid.utils.HydroidException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
public class CsvExtractorControllerTest {

   private MockMvc mockMvc;

   private CsvExtractorController csvExtractorController;

   @Mock
   private DocumentService documentService;

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
      csvExtractorController = new CsvExtractorController();
      ReflectionTestUtils.setField(csvExtractorController, "documentService", documentService);
      mockMvc = MockMvcBuilders.standaloneSetup(csvExtractorController).build();
   }

   @Test
   public void testMethodNotAllowed() {
      try {
         this.mockMvc.perform(
               MockMvcRequestBuilders.get("/import/bulk")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isMethodNotAllowed());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testInvalidRequest() {
      try {
         this.mockMvc.perform(
               MockMvcRequestBuilders.post("/import/bulk")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testEmptyFile() {
      try {
         this.mockMvc.perform(
               MockMvcRequestBuilders.fileUpload("/import/bulk")
                     .file("file", new byte[0])
                     .param("name", "test-file.csv"))
               .andExpect(status().isOk())
               .andExpect(content().string("{\"message\":\"You failed to upload test-file.csv because the file was empty.\",\"exception\":null}"));
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testSuccess() {
      try {
         this.mockMvc.perform(
               MockMvcRequestBuilders.fileUpload("/import/bulk")
                     .file("file", "http://www.test.com/xml".getBytes())
                     .param("name", "test-file.csv"))
               .andExpect(status().isOk())
               .andExpect(content().string("{\"message\":\"Your document has queued for enhancement successfully.\",\"exception\":null}"));
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

}