package au.gov.ga.hydroid.integration;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.service.DataObjectSummary;
import au.gov.ga.hydroid.service.S3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by u24529 on 8/02/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class S3ClientTestIT {

   @Autowired
   private S3Client s3Client;

   @Test
   public void testGetCredentials() {
      Assert.assertEquals("ga_aws_devs", s3Client.getAccountOwner());
   }

   @Test
   public void testStoreFile() {
      s3Client.storeFile("hydroid-output", "rdfs/first-file.rdf", "Sample content for rdf file", ContentType.APPLICATION_XML.getMimeType());
   }

   @Test
   public void testDeleteFile() {
      s3Client.deleteFile("hydroid", "enhancer/output/rdfs/first-file.rdf");
   }

   @Test
   public void testListObjects() {
      List<DataObjectSummary> objects = s3Client.listObjects("hydroid", "enhancer/input/");
      Assert.assertNotNull(objects);
      Assert.assertEquals("enhancer/input/", objects.get(0).getKey());
      for (DataObjectSummary objectSummary : objects) {
         System.out.println(objectSummary.getKey() + ": " + objectSummary.getBucketName());
      }
   }

   @Test
   public void testListObjectsWithWrongKey() {
      List<DataObjectSummary> objects = s3Client.listObjects("hydroid", "wrong/key");
      Assert.assertNotNull(objects);
      Assert.assertTrue(objects.isEmpty());
   }

}
