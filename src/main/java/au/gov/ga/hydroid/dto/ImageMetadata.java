package au.gov.ga.hydroid.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by u24529 on 16/03/2016.
 */
public class ImageMetadata {

   private List<String> tags;
   private List<ImageAnnotation> imageLabels;

   public ImageMetadata() {
      tags = new ArrayList<>();
      imageLabels = new ArrayList<>();
   }

   public List<String> getTags() {
      return tags;
   }

   public List<ImageAnnotation> getImageLabels() {
      return imageLabels;
   }

}
