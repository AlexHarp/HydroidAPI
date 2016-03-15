package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.service.JenaService;
import au.gov.ga.hydroid.utils.IOUtils;
import org.apache.jena.rdf.model.Statement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class JenaServiceTestIT {

   @Autowired
   private JenaService jenaService;

   private String rdfId;

   @Before
   public void initialize() {
      rdfId = "ID" + System.currentTimeMillis();
   }

   private void storeRdf() {
      InputStream rdfStream = this.getClass().getResourceAsStream("/testfiles/test.rdf");
      String rdfString = new String(IOUtils.fromInputStreamToByteArray(rdfStream));
      jenaService.storeRdf(rdfId, rdfString, "");
   }

   private void readRdfShouldExist() {
      List<Statement> model = jenaService.readRdf(rdfId);
      Assert.assertNotNull(model);
      Assert.assertThat(model.size(),greaterThan(0));
   }

   private void readRdfShouldNotExist() {
      List<Statement> model = jenaService.readRdf(rdfId);
      Assert.assertNull(model);
   }

   private void deleteRdf() {
      jenaService.deleteRdf(rdfId);
   }

   @Test
   public void testJenaService() {
      storeRdf();
      readRdfShouldExist();
      deleteRdf();
      readRdfShouldNotExist();
   }

   @Test
   public void testStoreRdfDefault() {
      InputStream rdfStream = this.getClass().getResourceAsStream("/testfiles/shoals.xml");
      String rdfString = new String(IOUtils.fromInputStreamToByteArray(rdfStream));
      jenaService.storeRdfDefault( rdfString, "");
   }

}