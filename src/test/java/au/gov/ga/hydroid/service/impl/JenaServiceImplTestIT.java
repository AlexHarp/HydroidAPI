package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.service.JenaService;
import au.gov.ga.hydroid.utils.IOUtils;
import com.hp.hpl.jena.rdf.model.Statement;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class JenaServiceImplTestIT {

    @Autowired
    private JenaService jenaService;

    @Test
    public void testStoreRdf() throws Exception {
      String randomId = "ID1455674323766";
      InputStream rdfStream = this.getClass().getResourceAsStream("/testfiles/test.rdf");
      String rdfString = new String(IOUtils.fromInputStreamToByteArray(rdfStream));
      jenaService.storeRdf(randomId, rdfString, "https://editor.vocabs.ands.org.au/");
    }

   @Test
   public void testReadRdf() throws Exception {
      List<Statement> model = jenaService.readRdf("ID1455674323766");
      Assert.assertNotNull(model);
   }

}