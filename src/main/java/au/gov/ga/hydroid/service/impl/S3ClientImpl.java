package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.service.S3Client;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Created by u24529 on 8/02/2016.
 */
@Service
public class S3ClientImpl implements S3Client {

   @Autowired
   private Environment environment;

   private AmazonS3 getAmazonS3() {
      if (environment.getProperty("proxy.host") != null) {
         ClientConfiguration clientConfiguration = new ClientConfiguration();
         clientConfiguration.setProxyHost("localhost");
         clientConfiguration.setProxyPort(3128);
         return new AmazonS3Client(clientConfiguration);
      } else {
         return new AmazonS3Client();
      }
   }

   @Override
   public String getAccountOwner() {
      AmazonS3 s3 = getAmazonS3();
      return s3.getS3AccountOwner().getDisplayName();
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
