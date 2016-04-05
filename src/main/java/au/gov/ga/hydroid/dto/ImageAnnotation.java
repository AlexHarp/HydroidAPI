package au.gov.ga.hydroid.dto;

import java.math.BigDecimal;

/**
 * Created by u24529 on 16/03/2016.
 */
public class ImageAnnotation {

   private String description;
   private float score;

   public ImageAnnotation(String description, float score) {
      this.description = description;
      this.score = score;
   }

   public String getDescription() {
      return description;
   }

   public float getScore() {
      return score;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ImageAnnotation that = (ImageAnnotation) o;

      if (Float.compare(that.score, score) != 0) return false;
      if (description != null ? !description.equals(that.description) : that.description != null) return false;

      return true;
   }

   @Override
   public int hashCode() {
      int result = description != null ? description.hashCode() : 0;
      result = 31 * result + (!BigDecimal.valueOf(score).equals(BigDecimal.ZERO) ? Float.floatToIntBits(score) : 0);
      return result;
   }

}
