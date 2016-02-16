package au.gov.ga.hydroid.model;

/**
 * Created by u24529 on 4/02/2016
 * Defines the Document object
 */
public class Document {

   private long id;
   private String urn;
   private String title;
   private DocumentType type;
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

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public DocumentType getType() {
      return type;
   }

   public void setType(DocumentType type) {
      this.type = type;
   }

   public byte[] getContent() {
      return content;
   }

   public void setContent(byte[] content) {
      this.content = content;
   }

}
