package au.gov.ga.hydroid.service;
import java.io.InputStream;
import java.util.List;

/**
 * Created by u24529 on 8/02/2016.
 */
public interface S3Client {

   public String getAccountOwner();
   public InputStream getFile(String bucketName, String key);
   public byte[] getFileAsByteArray(String bucketName, String key);
   public void storeFile(String bucketName, String key, String content, String contentType);
   public void storeFile(String bucketName, String key, InputStream content, String contentType);
   public void deleteFile(String bucketName, String key);
   public List<DataObjectSummary> listObjects(String bucketName, String key);
   public void copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey);

}
