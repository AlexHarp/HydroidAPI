package au.gov.ga.hydroid.model;

import java.util.Date;

/**
 * Created by u24529 on 4/02/2016
 * Defines the Document object
 */
public class Document {

   private long id;
   private String origin;
   private String urn;
   private String title;
   private DocumentType type;
   private EnhancementStatus status;
   private String statusReason;
   private Date processDate;

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

   public String getOrigin() {
      return origin;
   }

   public void setOrigin(String origin) {
      this.origin = origin;
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

   public EnhancementStatus getStatus() {
      return status;
   }

   public void setStatus(EnhancementStatus status) {
      this.status = status;
   }

   public String getStatusReason() {
      return statusReason;
   }

   public void setStatusReason(String statusReason) {
      this.statusReason = statusReason;
   }

   public Date getProcessDate() {
      return processDate;
   }

   public void setProcessDate(Date processDate) {
      this.processDate = processDate;
   }

}
