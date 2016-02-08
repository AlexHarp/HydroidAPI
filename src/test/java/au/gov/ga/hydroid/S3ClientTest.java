package au.gov.ga.hydroid;

import au.gov.ga.hydroid.service.S3Client;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by u24529 on 8/02/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
public class S3ClientTest {

   @Autowired
   private S3Client s3Client;

   @Test
   public void testGetCredentials() throws Exception {
      Assert.assertEquals("ga_aws_devs", s3Client.getAccountOwner());
   }

}
