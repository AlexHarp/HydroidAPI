package au.gov.ga.hydroid.utils;

import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Created by u24529 on 1/03/2016.
 */
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

   private static final Logger logger = LoggerFactory.getLogger(AutowiringSpringBeanJobFactory.class);
   private transient AutowireCapableBeanFactory beanFactory;

   @Override
   public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
      logger.debug("setApplicationContext - started");
      beanFactory = applicationContext.getAutowireCapableBeanFactory();
      logger.debug("setApplicationContext - finished");
   }

   @Override
   public Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
      logger.debug("createJobInstance - started");
      Object jobInstance = super.createJobInstance(bundle);
      beanFactory.autowireBean(jobInstance);
      logger.debug("createJobInstance - finished");
      return jobInstance;
   }

}
