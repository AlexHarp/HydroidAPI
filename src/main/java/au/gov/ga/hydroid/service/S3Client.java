package au.gov.ga.hydroid.service;

import java.io.InputStream;
import java.util.List;

/**
 * Created by u24529 on 8/02/2016.
 */
public interface S3Client {
   String getAccountOwner();

   InputStream getFile(String bucketName, String key);

   byte[] getFileAsByteArray(String bucketName, String key);

   void storeFile(String bucketName, String key, String content, String contentType);

   void storeFile(String bucketName, String key, InputStream content, String contentType);

   void deleteFile(String bucketName, String key);

   List<DataObjectSummary> listObjects(String bucketName, String key);

   void copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey);
}
