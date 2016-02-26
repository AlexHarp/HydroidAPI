package au.gov.ga.hydroid;

import au.gov.ga.hydroid.service.S3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Created by u24529 on 22/02/2016.
 */
public class CustomMockS3Client implements S3Client {

   @Override
   public String getAccountOwner() {
      return null;
   }

   @Override
   public InputStream getFile(String bucketName, String key) {
      return new ByteArrayInputStream(new byte[0]);
   }

   @Override
   public byte[] getFileAsByteArray(String bucketName, String key) {
      return new byte[0];
   }

   @Override
   public void storeFile(String bucketName, String key, String content, String contentType) {

   }

   @Override
   public void deleteFile(String bucketName, String key) {

   }

   @Override
   public List<S3ObjectSummary> listObjects(String bucketName, String key) {
      return null;
   }

}
