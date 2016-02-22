package au.gov.ga.hydroid;

import au.gov.ga.hydroid.service.S3Client;

/**
 * Created by u24529 on 22/02/2016.
 */
public class CustomMockS3Client implements S3Client {

   @Override
   public String getAccountOwner() {
      return null;
   }

   @Override
   public byte[] getFile(String bucketName, String key) throws Exception {
      return new byte[0];
   }

   @Override
   public void storeFile(String bucketName, String key, String content, String contentType) {

   }

   @Override
   public void deleteFile(String bucketName, String key) {

   }

}
