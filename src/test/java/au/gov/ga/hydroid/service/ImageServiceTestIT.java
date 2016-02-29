package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.HydroidApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;

/**
 * Created by u24529 on 25/02/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class ImageServiceTestIT {

   @Autowired
   private ImageService imageService;

   @Test
   public void testGetImageMetadata() {
      InputStream imageStream = this.getClass().getResourceAsStream("/testfiles/hydroid-3.jpg");
      String metadata = imageService.getImageMetadata(imageStream);

      String expectedMetadata = new StringBuilder("The Hydroid 3 Photo").append("\n")
            .append("Sub: The Hydroid 3 Photo").append("\n")
            .append("PhotoMedia").append("\n")
            .append("Difficult to parse").append("\n")
            .append("Hydroid").append("\n")
            .append("Hydrozoa").append("\n")
            .append("Jellyfish").append("\n")
            .append("Corals").append("\n")
            .append("Hydroid Hydrozoa Jellyfish Corals").append("\n")
            .toString();

      Assert.assertEquals(expectedMetadata, metadata);
   }

}
