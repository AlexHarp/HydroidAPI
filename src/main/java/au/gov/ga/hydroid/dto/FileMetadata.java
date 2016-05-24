package au.gov.ga.hydroid.dto;

import com.amazonaws.services.s3.model.ObjectMetadata;

/**
 * Created by u24529 on 24/05/2016.
 */
public class FileMetadata extends ObjectMetadata {

   @Override
   public long getInstanceLength() {
      return getContentLength();
   }

}
