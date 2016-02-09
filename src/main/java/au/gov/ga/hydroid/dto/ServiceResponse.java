package au.gov.ga.hydroid.dto;

/**
 * Created by u24529 on 8/02/2016.
 */
public class ServiceResponse {

   private String message;
   private String exception;

   public ServiceResponse() {
   }

   public ServiceResponse(String message) {
      this.message = message;
   }

   public ServiceResponse(String message, String exception) {
      this.message = message;
      this.exception = exception;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public String getException() {
      return exception;
   }

   public void setException(String exception) {
      this.exception = exception;
   }

}
