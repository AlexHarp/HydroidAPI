package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.mock.CustomMockS3Client;
import au.gov.ga.hydroid.service.S3Client;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HydroidApplication.class)
public class DownloadControllerTest {
   private MockMvc mockMvc;

   @Mock
   S3Client s3Client;

   @Autowired
   HydroidConfiguration configuration;

   DownloadController downloadController;

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
      downloadController = new DownloadController();
      ReflectionTestUtils.setField(downloadController, "s3Client", this.s3Client);
      ReflectionTestUtils.setField(downloadController, "configuration", this.configuration);
      mockMvc = MockMvcBuilders.standaloneSetup(downloadController).build();
   }

   @Test
   public void testDownload_NotFound() {
      try {
         this.mockMvc.perform(
               MockMvcRequestBuilders.get("/download/single/missing-urn")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testDownload_Found() {
      try {
         ReflectionTestUtils.setField(downloadController, "s3Client", new CustomMockS3Client());
         this.mockMvc.perform(
               MockMvcRequestBuilders.get("/download/single/urn1")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

}