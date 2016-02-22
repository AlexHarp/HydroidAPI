package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.CustomMockS3Client;
import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.S3Client;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HydroidApplication.class)
public class DocumentControllerTest {
    private MockMvc mockMvc;

    @Mock
    S3Client s3Client;

   @Mock
   HydroidConfiguration configuration;

    DocumentController documentController;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        documentController = new DocumentController();
        ReflectionTestUtils.setField(documentController, "s3Client", this.s3Client);
       ReflectionTestUtils.setField(documentController, "configuration", this.configuration);
        mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
    }

    @Test
    public void testDownload_NotFound() throws Exception {
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/document/missing-urn/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void testDownload_Found() throws Exception {
        ReflectionTestUtils.setField(documentController, "s3Client", new CustomMockS3Client());
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/document/urn1/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}