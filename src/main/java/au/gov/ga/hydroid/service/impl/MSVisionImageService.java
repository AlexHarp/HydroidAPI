package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.ImageAnnotation;
import au.gov.ga.hydroid.dto.ImageMetadata;
import au.gov.ga.hydroid.service.ImageService;
import au.gov.ga.hydroid.utils.HydroidException;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Service
public class MSVisionImageService implements ImageService {

   private static final Logger logger = LoggerFactory.getLogger(MSVisionImageService.class);
   private static final String MS_VISION_IMAGE_API_URL = "https://api.projectoxford.ai/vision/v1.0/";

   @Autowired
   private HydroidConfiguration configuration;

   private float round(float value, int newScale) {
      BigDecimal bd = BigDecimal.valueOf(value);
      bd = bd.setScale(newScale, BigDecimal.ROUND_HALF_UP);
      return bd.floatValue();
   }

   private HttpPost getHttpPost(String operation, int maxCandidates) throws URISyntaxException {
      URIBuilder builder = new URIBuilder(MS_VISION_IMAGE_API_URL + operation);
      builder.setParameter("maxCandidates", String.valueOf(maxCandidates));
      URI uri = builder.build();
      HttpPost request = new HttpPost(uri);
      if (configuration.getProxyPort() > 0) {
         HttpHost proxy = new HttpHost(configuration.getProxyHost(), configuration.getProxyPort());
         RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
         request.setConfig(config);
      }
      return request;
   }

   @SuppressWarnings("unchecked")
   @Override
   public ImageMetadata getImageMetadata(InputStream is) {

      HttpClient httpclient = HttpClients.createDefault();

      try {
         HttpPost request = getHttpPost("tag", 10);
         request.setHeader("Content-Type", ContentType.APPLICATION_OCTET_STREAM.getMimeType());
         request.setHeader("Ocp-Apim-Subscription-Key", System.getProperty("ms.vision.api.key"));

         InputStreamEntity reqEntity = new InputStreamEntity(is);
         request.setEntity(reqEntity);

         HttpResponse response = httpclient.execute(request);
         HttpEntity entity = response.getEntity();

         if (entity != null) {
            Gson gson = new Gson();
            Type jsonObjectType = new TypeToken<Map<String, Object>>(){}.getType();
            String responseData = EntityUtils.toString(entity);
            Map<String, Object> jsonObject = gson.fromJson(responseData, jsonObjectType);
            if (jsonObject != null) {
               final ImageMetadata imageMetadata = new ImageMetadata();
               List<LinkedTreeMap<String,Object>> tags = (List<LinkedTreeMap<String,Object>>) jsonObject.get("tags");
               tags.forEach((tag) -> {
                  imageMetadata.getImageLabels().add(
                     new ImageAnnotation((String) tag.get("name"), round(((Double) tag.get("confidence")).floatValue(), 2)));
               });
               return imageMetadata;
            }
         }

         return null;

      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @SuppressWarnings("unchecked")
   @Override
   public ImageMetadata describeImage(InputStream is) {
      HttpClient httpclient = HttpClients.createDefault();

      try {
         HttpPost request = getHttpPost("describe", 5);
         request.setHeader("Content-Type", ContentType.APPLICATION_OCTET_STREAM.getMimeType());
         request.setHeader("Ocp-Apim-Subscription-Key", System.getProperty("ms.vision.api.key"));

         InputStreamEntity reqEntity = new InputStreamEntity(is);
         request.setEntity(reqEntity);

         HttpResponse response = httpclient.execute(request);
         HttpEntity entity = response.getEntity();

         if (entity != null) {
            Gson gson = new Gson();
            Type jsonObjectType = new TypeToken<Map<String, Object>>(){}.getType();
            String responseData = EntityUtils.toString(entity);
            Map<String, Object> jsonObject = gson.fromJson(responseData, jsonObjectType);
            if (jsonObject != null) {
               final ImageMetadata imageMetadata = new ImageMetadata();

               LinkedTreeMap<String,Object> description = (LinkedTreeMap) jsonObject.get("description");

               // Simple tags
               List<String> tags = (List) description.get("tags");
               tags.forEach((tag) -> {
                  imageMetadata.getTags().add(tag);
               });

               // More detailed captions
               List<LinkedTreeMap<String,Object>> captions = (List<LinkedTreeMap<String,Object>>) description.get("captions");
               captions.forEach((caption) -> {
                  imageMetadata.getImageLabels().add(
                        new ImageAnnotation((String) caption.get("text"), round(((Double) caption.get("confidence")).floatValue(), 2)));
               });

               return imageMetadata;
            }
         }

         return null;

      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

}
