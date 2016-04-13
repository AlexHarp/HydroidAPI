package au.gov.ga.hydroid.integration;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.dto.ImageAnnotation;
import au.gov.ga.hydroid.dto.ImageMetadata;
import au.gov.ga.hydroid.service.impl.MSVisionImageService;
import org.imgscalr.Scalr;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class MicrosoftVisionTestIT {

   private static final Logger logger = LoggerFactory.getLogger(MicrosoftVisionTestIT.class);

   @Autowired
   private MSVisionImageService msVisionImageService;

   private InputStream getResizedImageStream(File imageFile) throws IOException {
      BufferedImage original = ImageIO.read(imageFile);
      BufferedImage resized = Scalr.resize(original, 1024);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(resized, "png", os);
      return new ByteArrayInputStream(os.toByteArray());
   }

   private void saveTextFile(String fileName, ImageMetadata imageMetadata) {
      String fileNameWithoutExt = fileName.substring(0, fileName.length() - 4);
      try (FileOutputStream os = new FileOutputStream("src/test/resources/testfiles/ms_image/" + fileNameWithoutExt + ".txt")) {
         BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
         if (!imageMetadata.getImageLabels().isEmpty()) {
            bw.write("Image Captions");
            bw.newLine();
            bw.write("--------------");
            bw.newLine();
            for (ImageAnnotation imageLabel : imageMetadata.getImageLabels()) {
               bw.write(imageLabel.getDescription() + "(" + imageLabel.getScore() + ")");
               bw.newLine();
            }
         }
         if (!imageMetadata.getTags().isEmpty()) {
            bw.write("Tags");
            bw.newLine();
            bw.write("--------------");
            bw.newLine();
            for (String tag : imageMetadata.getTags()) {
               bw.write(tag + ", ");
            }
         }
         bw.close();
      } catch (IOException e) {
         logger.error("saveTextFile - IOException: ", e);
      }
   }

   @Test
   public void testGetImageLabels() {
      InputStream imageStream = this.getClass().getResourceAsStream("/testfiles/hydroid-3.jpg");
      ImageMetadata result = msVisionImageService.getImageMetadata(imageStream);
      Assert.assertNotNull(result);
      Assert.assertTrue(result.getImageLabels().size() > 0);
   }

   @Test
   public void testGetImageCaptions() {
      try (InputStream imageStream = getResizedImageStream(new File("src/test/resources/testfiles/google_vision/CEAMARC_Stn79to88_IMG_9251_hydrocoral.JPG"))) {
         ImageMetadata result = msVisionImageService.describeImage(imageStream);
         Assert.assertNotNull(result);
         Assert.assertTrue(result.getTags().size() > 0);
         Assert.assertTrue(result.getImageLabels().size() > 0);
      } catch (Exception e) {
         logger.error("testGetImageCaptions - Exception: ", e);
      }
   }

   @Test
   public void testMicrosoftImageApiBatch() {
      try (Stream<Path> fileStream = Files.walk(Paths.get("src/test/resources/testfiles/google_vision"))) {
         fileStream.forEach(filePath -> {
            if (Files.isRegularFile(filePath) &&
                  (filePath.toString().toLowerCase().endsWith("jpg") || filePath.toString().toLowerCase().endsWith("png"))) {
               try (InputStream is = getResizedImageStream(filePath.toFile())) {
                  ImageMetadata result = msVisionImageService.describeImage(is);
                  saveTextFile(filePath.getFileName().toString(), result);
               } catch (Exception e) {
                  logger.error("testMicrosoftImageApiBatch - Exception: ", e);
               }
            }
         });
      } catch (IOException e) {
         logger.error("testMicrosoftImageApiBatch - IOException2: ", e);
      }
   }

}
