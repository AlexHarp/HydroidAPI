package au.gov.ga.hydroid;

import au.gov.ga.hydroid.job.EnhancerJob;
import au.gov.ga.hydroid.utils.AutowiringSpringBeanJobFactory;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

/**
 * Created by u24529 on 1/03/2016.
 */
@Configuration
@ConditionalOnProperty(name = "quartz.enabled")
public class SchedulerConfiguration {

   private static final Logger logger = LoggerFactory.getLogger(SchedulerConfiguration.class);

   @Bean
   public JobFactory jobFactory(ApplicationContext applicationContext) {
      logger.debug("jobFactory - started");
      AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
      jobFactory.setApplicationContext(applicationContext);
      logger.debug("jobFactory - finished");
      return jobFactory;
   }

   @Bean
   public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource, JobFactory jobFactory,
                                                    @Qualifier("enhancerJobTrigger") Trigger enhancerJobTrigger) throws IOException {
      logger.debug("schedulerFactoryBean - started");
      SchedulerFactoryBean factory = new SchedulerFactoryBean();
      // this allows to update triggers in DB when updating settings in config file:
      factory.setOverwriteExistingJobs(true);
      factory.setDataSource(dataSource);
      factory.setJobFactory(jobFactory);

      factory.setQuartzProperties(quartzProperties());
      factory.setTriggers(enhancerJobTrigger);

      logger.debug("schedulerFactoryBean - finished");
      return factory;
   }

   @Bean
   public Properties quartzProperties() throws IOException {
      logger.debug("quartzProperties - started");
      PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
      propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
      propertiesFactoryBean.afterPropertiesSet();
      logger.debug("quartzProperties - finished");
      return propertiesFactoryBean.getObject();
   }

   @Bean
   public JobDetailFactoryBean enhancerJobDetail() {
      logger.debug("enhancerJobDetail - started");
      JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
      factoryBean.setJobClass(EnhancerJob.class);
      factoryBean.setDurability(true);
      logger.debug("enhancerJobDetail - finished");
      return factoryBean;
   }

   @Bean(name = "enhancerJobTrigger")
   public SimpleTriggerFactoryBean sampleJobTrigger(@Qualifier("enhancerJobDetail") JobDetail jobDetail,
                                                    @Value("${enhancer.job.frequency}") long frequency) {

      logger.debug("sampleJobTrigger - started");
      SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
      factoryBean.setJobDetail(jobDetail);
      factoryBean.setStartDelay(0L);
      factoryBean.setRepeatInterval(Duration.ofMinutes(frequency).toMillis());
      factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
      factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
      logger.debug("sampleJobTrigger - finished");
      return factoryBean;
   }


}
