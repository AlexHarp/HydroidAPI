package au.gov.ga.hydroid.integration;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.dto.ImageAnnotation;
import au.gov.ga.hydroid.dto.ImageMetadata;
import au.gov.ga.hydroid.service.impl.GoogleVisionImageService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class GoogleVisionTestIT {

   private static final Logger logger = LoggerFactory.getLogger(GoogleVisionTestIT.class);

   @Autowired
   private GoogleVisionImageService googleVisionImageService;

   @Test
   public void testGoogleVisionApi() {
      Assert.assertNotNull("GOOGLE_APPLICATION_CREDENTIALS", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
      InputStream imageStream = this.getClass().getResourceAsStream("/testfiles/hydroid-3.jpg");
      ImageMetadata result = googleVisionImageService.getImageMetadata(imageStream);
      Assert.assertNotNull(result);
      Assert.assertTrue(result.getImageLabels().size() > 0);
   }

   private void saveTextFile(String fileName, List<ImageAnnotation> imageLabels) {
      String fileNameWithoutExt = fileName.substring(0, fileName.length() - 4);
      try (FileOutputStream os = new FileOutputStream("src/test/resources/testfiles/google_vision/" + fileNameWithoutExt + ".txt")) {
         BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
         for (ImageAnnotation imageLabel : imageLabels) {
            bw.write(imageLabel.getDescription() + "(" + imageLabel.getScore() + ")");
            bw.newLine();
         }
         bw.close();
      } catch (IOException e) {
         logger.error("saveTextFile - IOException: ", e);
      }
   }

   @Test
   public void testGoogleVisionApiBatch() {
      Assert.assertNotNull("GOOGLE_APPLICATION_CREDENTIALS", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
      try (Stream<Path> fileStream = Files.walk(Paths.get("src/test/resources/testfiles/google_vision"))) {
         fileStream.forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
               try (InputStream is = new FileInputStream(filePath.toFile())) {
                  ImageMetadata result = googleVisionImageService.getImageMetadata(is);
                  saveTextFile(filePath.getFileName().toString(), result.getImageLabels());
               } catch (IOException e) {
                  logger.error("testGoogleVisionApiBatch - IOException1: ", e);
               }
            }
         });
      } catch (IOException e) {
         logger.error("testGoogleVisionApiBatch - IOException2: ", e);
      }
   }

}
