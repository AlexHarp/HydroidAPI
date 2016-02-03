package au.gov.ga.hydroid;

import au.gov.ga.hydroid.service.EnhancerService;
import au.gov.ga.hydroid.service.impl.EnhancerServiceImpl;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testng.annotations.Test;

/**
 * Created by u24529 on 3/02/2016.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@WebIntegrationTest
public class EnhanherServiceTest {

   @Autowired
   private EnhancerService enhancerService;

   @Test
   public void testEnhance() throws Exception {
      //enhancerService = new EnhancerServiceImpl();
      enhancerService.enhance("This is a simple test for enhancement");
   }

}
