package au.gov.ga.hydroid.googlevision;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.service.impl.GoogleVisionImageService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class GoogleVisionTestIT {

   @Autowired
   private GoogleVisionImageService googleVisionImageService;

   @Test
   public void testGoogleVisionApi() throws IOException, GeneralSecurityException {
      InputStream imageStream = this.getClass().getResourceAsStream("/testfiles/hydroid-3.jpg");
      String result = googleVisionImageService.getImageMetadata(imageStream);
      Assert.assertNotNull(result);
      Assert.assertTrue(result.length() > 0);
   }
}
