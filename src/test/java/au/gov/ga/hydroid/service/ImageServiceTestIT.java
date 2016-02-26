package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.utils.IOUtils;
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
   public void testExtractRDFString() throws Exception {
      InputStream imageStream = this.getClass().getResourceAsStream("/testfiles/hydroid-3.jpg");
      String imageRDFString = imageService.extractRDFString(imageStream);

      InputStream rdfStream = this.getClass().getResourceAsStream("/testfiles/image-metadata.xml");
      String expectedRDF = new String(IOUtils.fromInputStreamToByteArray(rdfStream));

      // remove line breaks, tabs and white spaces before comparing
      expectedRDF = expectedRDF.replaceAll("\r\n", "");
      expectedRDF = expectedRDF.replaceAll(" ", "");
      expectedRDF = expectedRDF.replaceAll("\t", "");
      imageRDFString = imageRDFString.replaceAll("\r\n", "");
      imageRDFString = imageRDFString.replaceAll(" ", "");
      imageRDFString = imageRDFString.replaceAll("\t", "");

      Assert.assertEquals(expectedRDF, imageRDFString);
   }

   @Test
   public void testGetImageMetadata() throws Exception {
      InputStream imageStream = this.getClass().getResourceAsStream("/testfiles/hydroid-3.jpg");
      String metadata = imageService.getImageMetadata(imageStream);

      String expectedMetadata = new StringBuilder("PhotoMedia").append("\n")
            .append("Corals").append("\n")
            .append("Jellyfish").append("\n")
            .append("Hydrozoa").append("\n")
            .append("Hydroid").append("\n")
            .append("The Hydroid 3 Photo").append("\n").toString();

      Assert.assertEquals(expectedMetadata, metadata);
   }

}
