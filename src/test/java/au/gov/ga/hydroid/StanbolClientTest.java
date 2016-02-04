package au.gov.ga.hydroid;

import au.gov.ga.hydroid.service.StanbolClient;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Created by u24529 on 3/02/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
public class StanbolClientTest {

   @Autowired
   private StanbolClient stanbolClient;

   @Test
   public void testEnhance() throws Exception {
      stanbolClient.enhance("default", "Bob Barley is cool", StanbolMediaTypes.RDFXML);
   }

}
