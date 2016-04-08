package au.gov.ga.hydroid.integration;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.dto.ImageAnnotation;
import au.gov.ga.hydroid.dto.ImageMetadata;
import au.gov.ga.hydroid.service.impl.ImageServiceImpl;
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
   private ImageServiceImpl imageService;

   @Test
   public void testGetImageMetadata() {
      InputStream imageStream = this.getClass().getResourceAsStream("/testfiles/hydroid-3.jpg");
      ImageMetadata metadata = imageService.getImageMetadata(imageStream);

      StringBuilder actualMetadata = new StringBuilder();
      for (ImageAnnotation imageLabel : metadata.getImageLabels()) {
         actualMetadata.append(imageLabel.getDescription()).append("\n");
      }

      String expectedMetadata = new StringBuilder("The Hydroid 3 Photo").append("\n")
            .append("Sub: The Hydroid 3 Photo").append("\n")
            .append("Hydroid").append("\n")
            .append("PhotoMedia").append("\n")
            .append("Difficult to parse").append("\n")
            .append("Hydroid").append("\n")
            .append("Hydrozoa").append("\n")
            .append("Jellyfish").append("\n")
            .append("Corals").append("\n")
            .append("Hydroid Hydrozoa Jellyfish Corals").append("\n")
            .toString();

      Assert.assertEquals(expectedMetadata, actualMetadata.toString());
   }

}
