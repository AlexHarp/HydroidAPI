package au.gov.ga.hydroid.admintasks;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.RestClient;
import au.gov.ga.hydroid.service.impl.RestClientImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;

import static org.mockito.Matchers.isNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class FileImportTest {

   @Autowired
   private HydroidConfiguration configuration;

   @Test
   public void testFileUpload() throws Exception {
      RestClient client = new RestClientImpl(configuration);
      InputStream is = this.getClass().getResourceAsStream("/testfiles/Creativecommons-informational-flyer_eng.pdf");
      Response res = client.postFile(new URI("http://hydroid-dev-web-lb-1763223935.ap-southeast-2.elb.amazonaws.com/api/index-file"),"Creativecommons-informational-flyer_eng.pdf",is);
      Assert.assertNotNull(res);
      Assert.assertEquals(res.getStatus(),200);
   }
}
