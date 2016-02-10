package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.EnhancerService;
import org.apache.pdfbox.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HydroidApplication.class)
public class FileIndexControllerTest {

    private MockMvc mockMvc;

    @Mock
    HydroidConfiguration configuration;

    @Mock
    EnhancerService enhancerService;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        FileIndexController fileIndexController = new FileIndexController();
        ReflectionTestUtils.setField(fileIndexController, "enhancerService", this.enhancerService);
        ReflectionTestUtils.setField(fileIndexController, "configuration", this.configuration);
        mockMvc = MockMvcBuilders.standaloneSetup(fileIndexController).build();
    }

    @Test
    public void testUploadFile() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/testfiles/Creativecommons-informational-flyer_eng.pdf");
        byte[] fileBytes = IOUtils.toByteArray(is);
        MockMultipartFile firstFile = new MockMultipartFile("file",
                "Creativecommons-informational-flyer_eng.pdf",
                "application/pdf", fileBytes);
        this.mockMvc.perform(fileUpload("/index-file").file(firstFile).param("name", "mypdf.pdf"))
                .andExpect(status().isOk());

    }
}