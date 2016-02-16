package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.service.EnhancerService;
import au.gov.ga.hydroid.service.JenaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class JenaServiceImplTestIT {

    @Autowired
    private JenaService jenaService;

    @Autowired
    private EnhancerService enhancerService;

    @Test
    public void testStoreRdf() throws Exception {
        InputStream rdfStream = this.getClass().getResourceAsStream("/testfiles/test.rdf");
        jenaService.storeRdf(rdfStream,"https://editor.vocabs.ands.org.au/");
    }
}