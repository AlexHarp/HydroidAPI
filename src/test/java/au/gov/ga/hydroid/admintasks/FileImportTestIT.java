package au.gov.ga.hydroid.admintasks;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.utils.HydroidException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class FileImportTestIT {

   private static final Logger logger = LoggerFactory.getLogger(FileImportTestIT.class);

   private boolean isRegularFile(Path filePath) {
      try {
         return Files.isRegularFile(filePath) && (Files.size(filePath) < (1024 * 1024));
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   private void postFile(String uri, File file) {
      try {
         HttpClient httpClient = HttpClientBuilder.create().setProxy(new HttpHost("localhost", 3128)).build();
         HttpPost httppost = new HttpPost(uri);
         ContentBody cbFile = new FileBody(file);
         MultipartEntityBuilder builder = MultipartEntityBuilder.create();
         builder.addPart("file", cbFile);
         builder.addPart("name", new StringBody(file.getName(), ContentType.TEXT_PLAIN));

         httppost.setEntity(builder.build());
         logger.info("executing request " + httppost.getRequestLine());
         HttpResponse response = httpClient.execute(httppost);
         response.getEntity();

         Integer statusCode = response.getStatusLine().getStatusCode();
         if (statusCode != 200) {
            logger.info("File '" + file.getName() + "' failed with " + response.toString());
         } else {
            logger.info("File '" + file.getName() + "' succeeded.");
         }

      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testParseFile() {
      try (Stream<Path> fileStream = Files.walk(Paths.get("C:\\Data\\hydroid\\testing\\Articles\\Articles"))) {
         fileStream.forEach(filePath -> {
            if (isRegularFile(filePath)) {
               postFile("http://hydroid-dev-web-lb-1763223935.ap-southeast-2.elb.amazonaws.com/api/index-file", filePath.toFile());
            }
         });
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

}
