package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.service.S3Client;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Created by u24529 on 8/02/2016.
 */
@Service
public class S3ClientImpl implements S3Client {

   @Autowired
   private Environment environment;

   @Override
   public String getAccountOwner() {
      AmazonS3 s3 = null;

      if (environment.getProperty("proxy.host") != null) {
         ClientConfiguration clientConfiguration = new ClientConfiguration();
         clientConfiguration.setProxyHost("localhost");
         clientConfiguration.setProxyPort(3128);
         s3 = new AmazonS3Client(clientConfiguration);
      } else {
         s3 = new AmazonS3Client();
      }

      return s3.getS3AccountOwner().getDisplayName();
   }

   @Override
   public void storeFile(String bucketName, String key, String content) {

   }

}
