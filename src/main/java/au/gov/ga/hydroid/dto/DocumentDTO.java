package au.gov.ga.hydroid.dto;

import au.gov.ga.hydroid.model.DocumentType;

/**
 * Created by u24529 on 8/02/2016.
 */
public class DocumentDTO {

   public String title;
   public String content;
   public String docType = DocumentType.DOCUMENT.name();

}
