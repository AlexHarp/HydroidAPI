package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.S3Client;
import au.gov.ga.hydroid.utils.IOUtils;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by u24529 on 8/02/2016.
 */
@Service
public class S3ClientImpl implements S3Client {

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
   public byte[] getFile(String bucketName, String key) throws Exception {
      AmazonS3 s3 = getAmazonS3();
      S3Object object = s3.getObject(bucketName, key);
      InputStream is = object.getObjectContent();
      if (is != null) {
         return IOUtils.fromInputStreamToByteArray(is);
      }
      return null;
   }

   @Override
   public void storeFile(String bucketName, String key, String content, String contentType) {
      AmazonS3 s3 = getAmazonS3();

      // If the bucket doesn't exist we create it
      if (!s3.doesBucketExist(bucketName)) {
         s3.createBucket(bucketName, "ap-southeast-2");
      }

      InputStream fileContent = new ByteArrayInputStream(content.getBytes());
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType(contentType);

      s3.putObject(bucketName, key, fileContent, metadata);
   }

   @Override
   public void deleteFile(String bucketName, String key) {
      AmazonS3 s3 = getAmazonS3();
      if (s3.doesBucketExist(bucketName)) {
         s3.deleteObject(bucketName, key);
      }
   }

}
