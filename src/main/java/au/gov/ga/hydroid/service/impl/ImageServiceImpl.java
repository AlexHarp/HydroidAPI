package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.dto.ImageAnnotation;
import au.gov.ga.hydroid.dto.ImageMetadata;
import au.gov.ga.hydroid.service.ImageService;
import au.gov.ga.hydroid.utils.HydroidException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Created by u24529 on 26/02/2016.
 */
@Service
public class ImageServiceImpl implements ImageService {

   private static final String[] VALID_PROPERTIES = {"Author", "creator", "dc:creator",
         "dc:description", "dc:subject", "dc:title", "description", "Image Description",
         "Keywords", "meta:author", "meta:keyword", "subject", "Subject", "title",
         "Windows XP Comment", "Windows XP Keywords", "Windows XP Subject", "Windows XP Title"};

   @Override
   public ImageMetadata getImageMetadata(InputStream is) {
      ImageMetadata imageMetadata = new ImageMetadata();

      try {
         Parser parser = new AutoDetectParser();
         BodyContentHandler handler = new BodyContentHandler(-1);
         Metadata metadata = new Metadata();

         ParseContext context = new ParseContext();
         parser.parse(is, handler, metadata, context);

         // Collect valid property values add them to the result
         String[] metadataNames = metadata.names();
         if (metadataNames != null) {
            for (String propertyName : metadataNames) {
               if (ArrayUtils.indexOf(VALID_PROPERTIES, propertyName) >= 0) {
                  String propertyValue = metadata.get(propertyName);
                  // If propertyValue has multiple values, break it down into multiple lines
                  if (propertyValue.indexOf(";") >= 0) {
                     propertyValue = propertyValue.replaceAll(";", "\n");
                  }
                  ImageAnnotation imageLabel = new ImageAnnotation(propertyValue, 1);
                  // Only store unique values
                  if (!imageMetadata.getImageLabels().contains(imageLabel)) {
                     imageMetadata.getImageLabels().add(imageLabel);
                  }
               }
            }
         }

      } catch (Throwable e) {
         throw new HydroidException(e);
      }

      return imageMetadata;
   }

}
