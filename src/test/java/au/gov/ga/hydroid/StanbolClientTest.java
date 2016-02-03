package au.gov.ga.hydroid;

import au.gov.ga.hydroid.service.StanbolClient;
import au.gov.ga.hydroid.service.impl.StanbolClientImpl;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testng.annotations.Test;

/**
 * Created by u24529 on 3/02/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@WebIntegrationTest
public class StanbolClientTest {

   @Autowired
   private StanbolClient stanbolClient;
   /*
   @Mock
   private Page page;

   @BeforeMethod
   public void setupMock() {
      MockitoAnnotations.initMocks(this);
   }
   */

   @Test
   public void testEnhance() throws Exception {
      //stanbolClient = new StanbolClientImpl();
      stanbolClient.enhance("default", "Bob Barley is cool", StanbolMediaTypes.RDFXML);
   }

}
