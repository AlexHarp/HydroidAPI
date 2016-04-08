package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.mock.CustomMockStanbolClient;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

/**
 * Created by u24529 on 7/04/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
public class StanbolClientTest {

   private StanbolClient stanbolClient = new CustomMockStanbolClient();

   @Test
   public void testEnhance() {
      stanbolClient.enhance("default", "Bob Barley is cool", StanbolMediaTypes.RDFXML);
   }

   @Test
   public void testFindAllPredicates() {
      String enhancedText = stanbolClient.enhance("default", "This is the text for enhancement.", StanbolMediaTypes.RDFXML);
      Properties allPredicates = stanbolClient.findAllPredicates(enhancedText);
      Assert.assertNotNull(allPredicates);
      Assert.assertEquals(6, allPredicates.size());
      Assert.assertEquals("en", allPredicates.getProperty("language"));
   }

}
