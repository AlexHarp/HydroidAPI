package au.gov.ga.hydroid.googlevision;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.ImageService;
import au.gov.ga.hydroid.service.impl.GoogleVisionImageService;
import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hslf.blip.Bitmap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

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
}
