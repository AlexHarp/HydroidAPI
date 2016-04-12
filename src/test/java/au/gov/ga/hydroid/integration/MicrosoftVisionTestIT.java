package au.gov.ga.hydroid.integration;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.dto.ImageMetadata;
import au.gov.ga.hydroid.service.impl.MSVisionImageService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class MicrosoftVisionTestIT {

   private static final Logger logger = LoggerFactory.getLogger(MicrosoftVisionTestIT.class);

   @Autowired
   private MSVisionImageService msVisionImageService;

   @Test
   public void testGetImageLabels() {
      InputStream imageStream = this.getClass().getResourceAsStream("/testfiles/hydroid-3.jpg");
      ImageMetadata result = msVisionImageService.getImageMetadata(imageStream);
      Assert.assertNotNull(result);
      Assert.assertTrue(result.getImageLabels().size() > 0);
   }

   @Test
   public void testGetImageCaptions() {
      InputStream imageStream = this.getClass().getResourceAsStream("/testfiles/ophiuroid.jpg");
      ImageMetadata result = msVisionImageService.describeImage(imageStream);
      Assert.assertNotNull(result);
      Assert.assertTrue(result.getTags().size() > 0);
      Assert.assertTrue(result.getImageLabels().size() > 0);
   }

}
