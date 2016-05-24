package au.gov.ga.hydroid.mock;

import au.gov.ga.hydroid.service.DataObjectSummary;
import au.gov.ga.hydroid.service.S3Client;
import au.gov.ga.hydroid.service.impl.DataObjectSummaryImpl;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
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
   public void storeFile(String bucketName, String key, InputStream content, String contentType, long contentLength) {

   }

   @Override
   public void deleteFile(String bucketName, String key) {

   }

   @Override
   public List<DataObjectSummary> listObjects(String bucketName, String key) {
      List<DataObjectSummary> objects = new ArrayList<>();
      S3ObjectSummary object = new S3ObjectSummary();
      object.setBucketName("hydroid");
      object.setKey("file-test.txt");
      objects.add(new DataObjectSummaryImpl(object));
      return objects;
   }

   @Override
   public void copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {

   }

   @Override
   public ObjectMetadata getObjectMetadata(String bucketName, String key) {
      return null;
   }

}
