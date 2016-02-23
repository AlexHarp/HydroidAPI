package au.gov.ga.hydroid.admintasks;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.controller.FileIndexController;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.service.EnhancerService;
import au.gov.ga.hydroid.service.RestClient;
import au.gov.ga.hydroid.service.impl.RestClientImpl;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.apache.tika.exception.TikaException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Matchers.isNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class FileImportTest {

   @Autowired
   private HydroidConfiguration configuration;

   @Test
   public void testParseFile() throws TikaException, SAXException, IOException {

      Files.walk(Paths.get("C:\\Data\\hydroid\\testing\\Articles\\Articles")).forEach(filePath -> {
         try {
            if (Files.isRegularFile(filePath) && (Files.size(filePath) < (1024 * 1024))) {
              Integer repsponse = postFile("http://hydroid-dev-web-lb-1763223935.ap-southeast-2.elb.amazonaws.com/api/index-file",filePath.toFile());
               if(repsponse != 200)
                  System.out.print("File '" + filePath.toFile().getName() + "' failed with " + repsponse.toString());
               else
                  System.out.print("File '" + filePath.toFile().getName() + "' succeeded.");
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      });
   }

   private Integer postFile(String uri,File file) throws IOException {
      HttpClient httpClient = HttpClientBuilder.create().setProxy(new HttpHost("localhost",3128)).build();
      HttpPost httppost = new HttpPost(uri);
      ContentBody cbFile = new FileBody(file);
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.addPart("file", cbFile);
      builder.addPart("name",new StringBody(file.getName(), ContentType.TEXT_PLAIN));

      httppost.setEntity(builder.build());
      System.out.println("executing request " + httppost.getRequestLine());
      HttpResponse response = httpClient.execute(httppost);
      HttpEntity resEntity = response.getEntity();

      return response.getStatusLine().getStatusCode();
   }
}
