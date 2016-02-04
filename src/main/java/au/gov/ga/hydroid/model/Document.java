package au.gov.ga.hydroid.model;

/**
 * Created by u24529 on 4/02/2016.
 */
public class Document {

   private long id;
   private String urn;
   private byte[] content;

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getUrn() {
      return urn;
   }

   public void setUrn(String urn) {
      this.urn = urn;
   }

   public byte[] getContent() {
      return content;
   }

   public void setContent(byte[] content) {
      this.content = content;
   }

}
