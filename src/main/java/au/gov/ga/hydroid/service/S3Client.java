package au.gov.ga.hydroid.service;

/**
 * Created by u24529 on 8/02/2016.
 */
public interface S3Client {

   public String getAccountOwner();
   public void storeFile(String bucketName, String key, String content, String contentType);
   public void deleteFile(String bucketName, String key);

}
