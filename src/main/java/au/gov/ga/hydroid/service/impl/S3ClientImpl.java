package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.DataObjectSummary;
import au.gov.ga.hydroid.service.S3Client;
import au.gov.ga.hydroid.utils.IOUtils;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by u24529 on 8/02/2016.
 */
@Service
public class S3ClientImpl implements S3Client {

   private static final Logger logger = LoggerFactory.getLogger(S3ClientImpl.class);

   @Autowired
   private HydroidConfiguration configuration;

   private AmazonS3 getAmazonS3() {
      if (configuration.getProxyPort() > 0) {
         ClientConfiguration clientConfiguration = new ClientConfiguration();
         clientConfiguration.setProxyHost(configuration.getProxyHost());
         clientConfiguration.setProxyPort(configuration.getProxyPort());
         return new AmazonS3Client(new ProfileCredentialsProvider(), clientConfiguration);
      } else {
         return new AmazonS3Client(new ProfileCredentialsProvider());
      }
   }

   @Override
   public String getAccountOwner() {
      AmazonS3 s3 = getAmazonS3();
      return s3.getS3AccountOwner().getDisplayName();
   }

   @Override
   public InputStream getFile(String bucketName, String key)  {
      InputStream fileContent = null;
      try {
         AmazonS3 s3 = getAmazonS3();
         S3Object object = s3.getObject(bucketName, key);
         fileContent = object.getObjectContent();
      } catch (AmazonS3Exception e) {
         // No object with this key was found
         logger.warn("getFile - AmazonS3Exception: ", e);
      }
      return fileContent;
   }

   @Override
   public byte[] getFileAsByteArray(String bucketName, String key)  {
      InputStream is = getFile(bucketName, key);
      if (is != null) {
         return IOUtils.fromInputStreamToByteArray(is);
      }
      return null;
   }

   @Override
   public void storeFile(String bucketName, String key, String content, String contentType) {
      byte[] contentAsByteArray = content.getBytes();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(contentAsByteArray);
      storeFile(bucketName, key, inputStream, contentType, contentAsByteArray.length);
   }

   @Override
   public void storeFile(String bucketName, String key, InputStream content, String contentType, long contentLength) {
      AmazonS3 s3 = getAmazonS3();

      // If the bucket doesn't exist we create it
      if (!s3.doesBucketExist(bucketName)) {
         s3.createBucket(bucketName, "ap-southeast-2");
      }

      ObjectMetadata metadata = new ObjectMetadata();
      if (contentType != null) {
         metadata.setContentType(contentType);
      }
      metadata.setContentLength(contentLength);

      s3.putObject(bucketName, key, content, metadata);
   }

   @Override
   public void deleteFile(String bucketName, String key) {
      AmazonS3 s3 = getAmazonS3();
      if (s3.doesBucketExist(bucketName)) {
         s3.deleteObject(bucketName, key);
      }
   }

   @Override
   public List<DataObjectSummary> listObjects(String bucketName, String key) {
      List<DataObjectSummary> objects = new ArrayList();

      AmazonS3 s3 = getAmazonS3();

      ObjectListing objectListing = s3.listObjects(bucketName, key);

      do {
         for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            objects.add(new DataObjectSummaryImpl(objectSummary));
         }
         if (!objectListing.isTruncated()) {
            break;
         }
         objectListing = s3.listNextBatchOfObjects(objectListing);
      } while (true);

      return objects;
   }

   @Override
   public void copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
      AmazonS3 s3 = getAmazonS3();
      s3.copyObject(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
   }

   @Override
   public ObjectMetadata getObjectMetadata(String bucketName, String key) {
      AmazonS3 s3 = getAmazonS3();
      return s3.getObjectMetadata(bucketName, key);
   }

}
