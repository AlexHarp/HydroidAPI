package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.mock.CustomMockDocumentService;
import au.gov.ga.hydroid.mock.CustomMockS3Client;
import au.gov.ga.hydroid.service.DocumentService;
import au.gov.ga.hydroid.service.S3Client;
import au.gov.ga.hydroid.utils.HydroidException;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
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
public class DownloadControllerTest {

   private MockMvc mockMvc;

   @Mock
   private S3Client s3Client;

   @Autowired
   HydroidConfiguration configuration;

   DownloadController downloadController;

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
      downloadController = new DownloadController();
      ReflectionTestUtils.setField(downloadController, "s3Client", this.s3Client);
      ReflectionTestUtils.setField(downloadController, "configuration", this.configuration);
      ReflectionTestUtils.setField(downloadController, "documentService", new CustomMockDocumentService());
      mockMvc = MockMvcBuilders.standaloneSetup(downloadController).build();
   }

   @Test
   public void testDownloadNotFound() {
      try {
         this.mockMvc.perform(
               MockMvcRequestBuilders.get("/download/rdfs/missing-urn")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testDownloadRDF() {
      try {
         ReflectionTestUtils.setField(downloadController, "s3Client", new CustomMockS3Client());
         this.mockMvc.perform(
               MockMvcRequestBuilders.get("/download/rdfs/urn1")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(StanbolMediaTypes.RDFXML_STRING))
               .andExpect(status().isOk())
               .andExpect(content().contentType(StanbolMediaTypes.RDFXML_STRING));
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testDownloadDocument() {
      try {
         ReflectionTestUtils.setField(downloadController, "s3Client", new CustomMockS3Client());
         this.mockMvc.perform(
               MockMvcRequestBuilders.get("/download/documents/urn1")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_OCTET_STREAM))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testDownloadInternalServerError() {
      try {
         ReflectionTestUtils.setField(downloadController, "s3Client", null);
         this.mockMvc.perform(
               MockMvcRequestBuilders.get("/download/rdfs/urn1")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isInternalServerError());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testDownloadBundleOk() {
      try {
         ReflectionTestUtils.setField(downloadController, "s3Client", new CustomMockS3Client());
         this.mockMvc.perform(
               MockMvcRequestBuilders.get("/download/bundle/urn1,urn2")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testDownloadBundleEmpty() {
      try {
         ReflectionTestUtils.setField(downloadController, "s3Client", null);
         this.mockMvc.perform(
               MockMvcRequestBuilders.get("/download/bundle/urn1,urn2")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().string("No files were found or bundled for download."));
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testDownloadImageOk() {
      try {
         ReflectionTestUtils.setField(downloadController, "s3Client", new CustomMockS3Client());
         this.mockMvc.perform(
               MockMvcRequestBuilders.get("/download/images/urn1")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testDownloadImageInternalServerError() {
      try {
         ReflectionTestUtils.setField(downloadController, "s3Client", null);
         this.mockMvc.perform(
               MockMvcRequestBuilders.get("/download/images/urn1")
                     .contentType(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isInternalServerError());
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

}