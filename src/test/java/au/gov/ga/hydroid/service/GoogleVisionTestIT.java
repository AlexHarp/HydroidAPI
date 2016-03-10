package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.HydroidApplication;
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
      InputStream imageStream = this.getClass().getResourceAsStream("/testfiles/hydroid-3.jpg");
      String result = googleVisionImageService.getImageMetadata(imageStream);
      Assert.assertNotNull(result);
      Assert.assertTrue(result.length() > 0);
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
                  String result = googleVisionImageService.getImageMetadata(is);
                  fileNameWithoutExt = filePath.getFileName().toString().substring(0, filePath.getFileName().toString().length() - 4);
                  os = new FileOutputStream("src/test/resources/testfiles/google_vision/" + fileNameWithoutExt + ".txt");
                  os.write(result.getBytes());
               }
            } catch (Throwable e) {
               e.printStackTrace();
            } finally {
               try {
                  if (is != null) is.close();
                  if (os != null) os.close();
               } catch (IOException e) {
               }
            }
         });
      } catch (Throwable e) {
         e.printStackTrace();
      }
   }

}
