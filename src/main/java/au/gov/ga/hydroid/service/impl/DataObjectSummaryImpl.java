package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.service.DataObjectSummary;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * Created by u31532 on 11/04/2016.
 */
public class DataObjectSummaryImpl implements DataObjectSummary {

   public DataObjectSummaryImpl(S3ObjectSummary s3ObjectSummary) {
      key = s3ObjectSummary.getKey();
      bucketName = s3ObjectSummary.getBucketName();
   }

   public DataObjectSummaryImpl(String bucketName,String key) {
      this.key = key;
      this.bucketName = bucketName;
   }

   private String key;
   private String bucketName;

   @Override
   public String getKey() {
      return key;
   }

   @Override
   public String getBucketName() {
      return bucketName;
   }
}
