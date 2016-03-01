package au.gov.ga.hydroid.utils;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Created by u24529 on 1/03/2016.
 */
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

   private transient AutowireCapableBeanFactory beanFactory;

   @Override
   public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
      beanFactory = applicationContext.getAutowireCapableBeanFactory();
   }

   @Override
   public Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
      final Object jobInstance = super.createJobInstance(bundle);
      beanFactory.autowireBean(jobInstance);
      return jobInstance;
   }

}
