package au.gov.ga.hydroid.utils;

/**
 * Created by u24529 on 25/02/2016.
 */
public class HydroidException extends RuntimeException {

   public HydroidException(String message) {
      super(message);
   }

   public HydroidException(Throwable cause) {
      super(cause.getMessage(), cause);
   }

}
