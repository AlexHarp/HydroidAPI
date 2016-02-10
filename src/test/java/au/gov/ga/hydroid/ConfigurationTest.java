package au.gov.ga.hydroid;

import org.junit.Assert;
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
public class ConfigurationTest {

   @Autowired
   private HydroidConfiguration configuration;

   @Test
   public void testConfiguration() throws Exception {
      Assert.assertNotNull(configuration);
      Assert.assertEquals(3128, configuration.getProxyPort());
      Assert.assertEquals("default", configuration.getStanbolChain());
      Assert.assertEquals("http://hydroid-dev-web-lb-1763223935.ap-southeast-2.elb.amazonaws.com/stanbol/enhancer/chain/", configuration.getStanbolUrl());
   }

}
