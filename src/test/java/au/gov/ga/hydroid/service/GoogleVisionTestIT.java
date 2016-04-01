package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.dto.ImageAnnotation;
import au.gov.ga.hydroid.dto.ImageMetadata;
import au.gov.ga.hydroid.service.impl.GoogleVisionImageService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class GoogleVisionTestIT {

   @Autowired
   private GoogleVisionImageService googleVisionImageService;

   @Test
   public void testGoogleVisionApi() throws IOException, GeneralSecurityException {
      Assert.assertNotNull("GOOGLE_APPLICATION_CREDENTIALS", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
      InputStream imageStream = this.getClass().getResourceAsStream("/testfiles/hydroid-3.jpg");
      ImageMetadata result = googleVisionImageService.getImageMetadata(imageStream);
      Assert.assertNotNull(result);
      Assert.assertTrue(result.getImageLabels().size() > 0);
   }

   @Test
   public void testGoogleVisionApiBatch() throws IOException, GeneralSecurityException {
      Assert.assertNotNull("GOOGLE_APPLICATION_CREDENTIALS", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
      try {
         Files.walk(Paths.get("src/test/resources/testfiles/google_vision")).forEach(filePath -> {
            InputStream is = null;
            OutputStream os = null;
            try {
               String fileNameWithoutExt;
               if (Files.isRegularFile(filePath)) {
                  is = new FileInputStream(filePath.toFile());
                  ImageMetadata result = googleVisionImageService.getImageMetadata(is);
                  fileNameWithoutExt = filePath.getFileName().toString().substring(0, filePath.getFileName().toString().length() - 4);
                  os = new FileOutputStream("src/test/resources/testfiles/google_vision/" + fileNameWithoutExt + ".txt");
                  BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                  for (ImageAnnotation imageLabel : result.getImageLabels()) {
                     bw.write(imageLabel.getDescription() + "(" + imageLabel.getScore() + ")");
                     bw.newLine();
                  }
                  bw.close();
               }
            } catch (Exception e) {
               e.printStackTrace();
            } finally {
               try {
                  if (is != null) is.close();
                  if (os != null) os.close();
               } catch (IOException e) {
               }
            }
         });
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
