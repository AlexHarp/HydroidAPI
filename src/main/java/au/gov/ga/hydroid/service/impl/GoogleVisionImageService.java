package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.ImageService;
import au.gov.ga.hydroid.utils.HydroidException;
import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

@Service
public class GoogleVisionImageService implements ImageService {

   private Logger logger = LoggerFactory.getLogger(getClass());

   @Autowired
   private HydroidConfiguration configuration;

   @Override
   public String extractRDFString(InputStream is) {
      return getImageMetadata(is);
   }

   @Override
   public String getImageMetadata(InputStream is) {
      if(is == null) {
         logger.debug("GoogleVisionImageService:getImageMetadata - input stream is null.");
         throw new HydroidException("input stream is null");
      }
      try {
         JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
         Vision.Builder builder;

         builder = new Vision.Builder(newProxyTransport(configuration), jsonFactory, null);

         String apiKey = configuration.getGoogleVisionApiKey();
         if(apiKey == null || apiKey.length() == 0) {
            throw new HydroidException("Google Vision API key missing from configuration. Check ${google.vision.apiKey}.");
         }
         builder.setVisionRequestInitializer(new
                 VisionRequestInitializer());
         Vision vision = builder.build();

         BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                 new BatchAnnotateImagesRequest();
         batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            Image base64EncodedImage = new Image();

            byte[] bytes = new byte[0];
            bytes = IOUtils.toByteArray(is);

            base64EncodedImage.encodeContent(bytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
               Feature labelDetection = new Feature();
               labelDetection.setType("LABEL_DETECTION");
               labelDetection.setMaxResults(10);
               add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
         }});

         Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);

         // Due to a bug: requests to Vision API containing large images fail when GZipped.
         annotateRequest.setDisableGZipContent(true);

         BatchAnnotateImagesResponse response;

         response = annotateRequest.execute();

         StringBuilder result = new StringBuilder();
         for (AnnotateImageResponse imgRes : response.getResponses()) {
            for (EntityAnnotation entityAnnotation : imgRes.getLabelAnnotations()) {
               result.append(entityAnnotation.getDescription()).append(",");
            }
         }
         return result.substring(0, result.length() - 1);
      }
      catch (Throwable e) {
         throw new HydroidException(e);
      }
   }

   static HttpTransport newProxyTransport(HydroidConfiguration configuration) throws GeneralSecurityException, IOException {
      NetHttpTransport.Builder builder = new NetHttpTransport.Builder();
      builder.trustCertificates(GoogleUtils.getCertificateTrustStore());
      if(configuration.getProxyPort() > 0) {
         builder.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(configuration.getProxyHost(), configuration.getProxyPort())));
      }
      return builder.build();
   }
}
