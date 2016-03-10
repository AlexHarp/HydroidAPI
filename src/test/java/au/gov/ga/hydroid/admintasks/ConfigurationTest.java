package au.gov.ga.hydroid.admintasks;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
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
   public void testConfiguration() {
      Assert.assertNotNull(configuration);
      Assert.assertEquals(3128, configuration.getProxyPort());
      Assert.assertEquals("default", configuration.getStanbolChain());
      Assert.assertEquals("http://hydroid-dev-stanbol-lb-2008994174.ap-southeast-2.elb.amazonaws.com/stanbol/enhancer/chain/", configuration.getStanbolUrl());
   }

}
