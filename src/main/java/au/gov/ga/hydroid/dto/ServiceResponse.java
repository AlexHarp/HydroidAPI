package au.gov.ga.hydroid.dto;

/**
 * Created by u24529 on 8/02/2016.
 */
public class ServiceResponse {

   private String message;

   public ServiceResponse() {
   }

   public ServiceResponse(String message) {
      this.message = message;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

}
