package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.DocumentDTO;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.service.EnhancerService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.jboss.resteasy.mock.MockHttpRequest.post;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HydroidApplication.class)
public class EnhancerControllerTest {    private MockMvc mockMvc;

    @Mock
    HydroidConfiguration configuration;

    @Mock
    EnhancerService enhancerService;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        EnhancerController enhancerController = new EnhancerController();
        ReflectionTestUtils.setField(enhancerController, "enhancerService", this.enhancerService);
        ReflectionTestUtils.setField(enhancerController, "configuration", this.configuration);
        mockMvc = MockMvcBuilders.standaloneSetup(enhancerController).build();
    }

    @Test
    public void testEnhance() throws Exception {
        DocumentDTO request =new DocumentDTO();
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
    }
}